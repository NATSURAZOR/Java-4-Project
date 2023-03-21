
package com.example.project;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Pair;
import javax.swing.*;
import java.awt.geom.Line2D;
import java.util.*;

public class Geoboard extends Application {
    final double simulationSpeed = 10;
    double screenWidth = 1280;
    double screenHeight = 720;
    double gameboardWidth = screenWidth * 0.546875;
    double gameboardHeight = screenHeight * 0.97222222222;
    int defaultRow = 4;
    int defaultColumn = 4;
    int countOfRow;
    int countOfColumn;
    List<OnePosition> listOPosition = new ArrayList<>();
    List<RubberBand> listOfRubberBands = new ArrayList<>();
    PlayGround playGround;
    boolean resetGame = false;
    OnePosition pos1MoveLine;
    OnePosition pos2MoveLine;
    RubberBand nowUsedRubberBand;
    List<RubberLine> nowUsedRubberLines = new ArrayList<>();
    RubberLine nowUsedRubberLine;
    boolean youLose = false;
    Rectangle changeColor;
    boolean somethingInput = false;
    int lastClickedRubbenGumID = -1;
    double globalobvod = 0;
    double globalobsah = 0;

    public class OnePosition{
        int[] id;
        double x, y;
        double width, height;
        double r;
        double[] center = new double[]{0, 0};
        Color color;
        Rectangle rec;
        Ellipse radius;
        Ellipse dot;

        public OnePosition(int[] id, Color color) {
            this.id = id;
            this.color = color;
            width = gameboardWidth / countOfRow;
            height = gameboardHeight / countOfColumn;
            x = screenWidth * 0.01 + id[0] * width;
            y = screenHeight * 0.01 + id[1] * height;
            center[0] = x + width / 2;
            center[1] = y + height / 2;
            rec = new Rectangle(x, y, width, height);
            radius = new Ellipse(center[0], center[1], width / 2, height / 2);
            dot = new Ellipse(center[0], center[1], width * 0.1, height * 0.1);
        }

        public void paint(){
            rec = new Rectangle(x, y, width, height);
            rec.setFill(color);
            rec.setStroke(Color.BLACK);

            radius = new Ellipse(center[0], center[1], width / 2, height / 2);
            radius.setFill(Color.BLACK);

            dot = new Ellipse(center[0], center[1], width * 0.1, height * 0.1);
            dot.setFill(Color.BLUE);
            dot.setStroke(Color.BLACK);
            playGround.getChildren().addAll(rec, radius, dot);
        }

        public boolean rubberBandInPos(double rbx, double rby){
            return length2points(center[0], center[1], rbx, rby) <= radius.getRadiusX() && length2points(center[0], center[1], rbx, rby) <= radius.getRadiusY();
        }

        public Pair<List<RubberLine>, RubberBand> clikedonMe(double mouseX, double mouseY){
            if (length2points(dot.getCenterX(), dot.getCenterY(), mouseX, mouseY) < dot.getRadiusX() &&
                length2points(dot.getCenterX(), dot.getCenterY(), mouseX, mouseY) < dot.getRadiusY()){

                List<RubberLine> clikedRubberLine = new ArrayList<>();

                for (RubberBand rubberBand : listOfRubberBands){
                    for (RubberLine rubberLine : rubberBand.listOfRubberLine){
                        if (rubberLine.pos1.id == id || rubberLine.pos2.id == id){
                            clikedRubberLine.add(rubberLine);
                        }
                    }
                    if (clikedRubberLine.size() == 2){
                        return new Pair<List<RubberLine>, RubberBand>(clikedRubberLine, rubberBand);
                    }
                }

                return null;
            }
            return null;
        }

        public void update(){

        }

        public void scaleUpX(double factor){
            x *= factor;
            width *= factor;
            center[0] *= factor;
        }

        public void scaleUpY(double factor){
            y *= factor;
            height *= factor;
            center[1] *= factor;
        }
    }

    public class RubberBand{
        List<RubberLine> listOfRubberLine = new ArrayList<>();
        List<RubberLine> hidenRubberLines = new ArrayList<>();
        RubberLine hidenLine;
        int id;
        double x, y, width, length;
        Color color;
        Line line1, line2;
        Circle clickedDot;
        OnePosition nowUsedPos1;
        OnePosition nowUsedPos2;

        public RubberBand(int id, double x, double y, Color color) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.color = color;
            this.length = screenHeight * 0.3;

        }

        public double getObvod(){
            double result = 0;

            for (RubberLine rubberLine: listOfRubberLine){
                result += rubberLine.lengthBetweenTwoDot;
            }

            return result;
        }

        public double getObsah(){
            if (listOfRubberLine.size() < 2){
                return 0;
            }

            List<RubberLine> copyList = new ArrayList<>(listOfRubberLine);
            double[] firstValue = new double[]{copyList.get(0).pos1.center[0], copyList.get(0).pos1.center[1]};
            double[] waitedValue = new double[]{copyList.get(0).pos2.center[0], copyList.get(0).pos2.center[1]};
            copyList.remove(0);

            List<double[][]> listOfDots = new ArrayList<>();
            listOfDots.add(new double[][]{firstValue, waitedValue});

            while (!copyList.isEmpty()){
                for (RubberLine rubberLine : copyList){
                    double[] pos1Value = new double[]{rubberLine.pos1.center[0], rubberLine.pos1.center[1]};
                    double[] pos2Value = new double[]{rubberLine.pos2.center[0], rubberLine.pos2.center[1]};



                    if (Arrays.equals(pos1Value, waitedValue)){
                        listOfDots.add(new double[][]{waitedValue, pos2Value});
                        waitedValue = pos2Value;
                        copyList.remove(rubberLine);
                        break;
                    }

                    if (Arrays.equals(pos2Value, waitedValue)){
                        listOfDots.add(new double[][]{waitedValue, pos1Value});
                        waitedValue = pos1Value;
                        copyList.remove(rubberLine);
                        break;
                    }
                }
            }

//            listOfDots.add(listOfDots.get(0));

            int sum1 = 0;
            int sum2 = 0;

            for (double[][] pos : listOfDots) {
                double x1 = pos[0][0];
                double y1 = pos[0][1];

                double x2 = pos[1][0];
                double y2 = pos[1][1];

                sum1 += x1 * y2;
                sum2 += y1 * x2;
            }


            return  Math.abs(sum1 - sum2) / 2d;
        }

        public void paint(){
            if (listOfRubberLine.isEmpty()){
                Line line = new Line(x, y, x, y + length);
                line.setStroke(color);
                line.setStrokeWidth(screenWidth * 0.01);

                playGround.getChildren().add(line);
                return;
            }

            for (RubberLine rubberLine : listOfRubberLine){
                rubberLine.paint();
            }

            if (clickedDot != null){
                playGround.getChildren().addAll(clickedDot, line1, line2);
            }

        }

        public void update(double mouseX, double mouseY){
            x = (mouseX - screenWidth*0.01/2);
            y = mouseY - length / 2;
        }

        public void changeColor(String newcolor){
            try {
                color = Color.valueOf(newcolor);
                listOfRubberLine.forEach(rubberLine -> rubberLine.color = Color.valueOf(newcolor));
            } catch (Exception e){
                System.out.println("bad color");
            }
        }

        public void createFigures(double mouseX, double mouseY, OnePosition pos1, OnePosition pos2){
            clickedDot = new Circle(mouseX, mouseY, 20);
            clickedDot.setFill(color);
            line1 = new Line(pos1.center[0], pos1.center[1], clickedDot.getCenterX(), clickedDot.getCenterY());
            line1.setStroke(color);
            line1.setStrokeWidth(screenWidth * 0.01);
            line2 = new Line(pos2.center[0], pos2.center[1], clickedDot.getCenterX(), clickedDot.getCenterY());
            line2.setStroke(color);
            line2.setStrokeWidth(screenWidth * 0.01);
        }

        public void updateStart(double mouseX, double mouseY){
            RubberLine rubberLine = listOfRubberLine.get(0);
            pos1MoveLine = rubberLine.pos1;
            pos2MoveLine = rubberLine.pos2;
            createFigures(mouseX, mouseY, pos1MoveLine, pos2MoveLine);
        }

        public void update2(double mouseX, double mouseY){
            createFigures(mouseX, mouseY, pos1MoveLine, pos2MoveLine);
        }

        public void update2Lines(List<RubberLine> clikedRubberLine, double mouseX, double mouseY){
            RubberLine rubberLine1 = clikedRubberLine.get(0);
            RubberLine rubberLine2 = clikedRubberLine.get(1);

            nowUsedPos1 = rubberLine1.pos1;
            nowUsedPos2 = rubberLine2.pos1;

            if (rubberLine1.pos1.id == rubberLine2.pos1.id || rubberLine1.pos1.id == rubberLine2.pos2.id){
                nowUsedPos1 = rubberLine1.pos2;
            }

            if (rubberLine2.pos1.id == rubberLine1.pos1.id || rubberLine2.pos1.id == rubberLine1.pos2.id){
                nowUsedPos2 = rubberLine2.pos2;
            }

            createFigures(mouseX, mouseY, nowUsedPos1, nowUsedPos2);
        }

        public void clearMoveObjects(){
            clickedDot = null;
            line1 = null;
            line2 = null;
            playGround.paint();
        }

        public boolean inPos(){
            if (listOfRubberLine.isEmpty()){
                for (OnePosition pos : listOPosition){
                    if (pos.rubberBandInPos(x, y) && pos.id[1] != countOfRow - 1){
                        OnePosition a = listOPosition.get((pos.id[1] + 1) * countOfColumn + pos.id[0]);
                        listOfRubberLine.add(new RubberLine(a, pos, color));
                        return true;
                    }
                }
                return false;
            }

            if (clickedDot != null && nowUsedRubberLines.isEmpty() && nowUsedRubberLine != null) {
                for (OnePosition pos : listOPosition) {
                    if (pos.rubberBandInPos(clickedDot.getCenterX(), clickedDot.getCenterY()) &&
                            pos.id != nowUsedRubberLine.pos1.id && pos.id != nowUsedRubberLine.pos2.id) {
                        listOfRubberLine.add(new RubberLine(nowUsedRubberLine.pos2, pos, color));
                        listOfRubberLine.add(new RubberLine(pos, nowUsedRubberLine.pos1, color));
                        return true;
                    }
                }
            }

            if (clickedDot != null && !nowUsedRubberLines.isEmpty() && nowUsedRubberLine == null){
                for (OnePosition pos : listOPosition){
                    if (pos.rubberBandInPos(clickedDot.getCenterX(), clickedDot.getCenterY()) &&
                            pos.id != nowUsedRubberLines.get(0).pos1.id && pos.id != nowUsedRubberLines.get(0).pos2.id &&
                            pos.id != nowUsedRubberLines.get(1).pos1.id && pos.id != nowUsedRubberLines.get(1).pos2.id){
                        listOfRubberLine.add(new RubberLine(nowUsedPos2, pos, color));
                        listOfRubberLine.add(new RubberLine(pos, nowUsedPos1, color));
                        return true;
                    }
                }
            }

            return false;
        }

        public boolean clikedonMe(double mouseX, double mouseY){
            if (listOfRubberLine.isEmpty() || listOfRubberLine.size() == 1){
                if (clickedDot == null){
                    System.out.println("Clicked dot is null. Check line");
                    return cliked(x, y, mouseX, mouseY);
                }
                System.out.println("cliked dot is not null. Check dot and line");
                if (nowUsedRubberBand == this){
                    return length2points(clickedDot.getCenterX(), clickedDot.getCenterY(), mouseX, mouseY) <= 20;
                }

            }
            return false;
        }

        public RubberLine clikedOnMeForlistOfRubberLine(double mouseX, double mouseY){
            for (RubberLine rubberLine : listOfRubberLine){
                if (clickedDot == null ){
                   if( rubberLine.clikedOnMe(mouseX, mouseY) ){
                       System.out.println("clikedOnMeForlistOfRubberLine cfot is NULL");
                       pos1MoveLine = rubberLine.pos1;
                       pos2MoveLine = rubberLine.pos2;
                       hidenLine = rubberLine;
                       listOfRubberLine.remove(rubberLine);
                       return rubberLine;
                    }
                }
                if (clickedDot != null && nowUsedRubberBand == this){
                    if (length2points(clickedDot.getCenterX(), clickedDot.getCenterY(), mouseX, mouseY) <= 20){
                        System.out.println("clikedOnMeForlistOfRubberLine check dot");
                        return rubberLine;
                    }
                }
            }
            return null;
        }

        public boolean cliked(double x, double y, double mouseX, double mouseY){
            double dx1 = screenWidth * 0.01;
            double dy1 = length;

            double dx = mouseX - x;
            double dy = mouseY - y;

            double s = dx1 * dy - dx * dy1;

            double ab = Math.sqrt(dx1 * dx1 + dy1 * dy1);
            double h = s / ab;

            return Math.abs(h) < (screenWidth * 0.01) / 2;
        }

        public void scaleUpX(double factor){
            x *= factor;
            width *= factor;
        }

        public void scaleUpY(double factor){
            y *= factor;
            length *= factor;
        }
    }

    public double length2points(double x1, double y1, double x2, double y2){
        return  Math.sqrt(Math.pow(x1-x2, 2) + Math.pow(y1-y2, 2));
    }

    public class RubberLine{
        OnePosition pos1;
        OnePosition pos2;
        Color color;
        double lengthBetweenTwoDot;

        public RubberLine(OnePosition pos1, OnePosition pos2, Color color) {
            this.pos1 = pos1;
            this.pos2 = pos2;
            this.color = color;
            lengthBetweenTwoDot = length2points(pos1.center[0], pos1.center[1], pos2.center[0], pos2.center[1]);
        }

        public void paint(){
            Line line = new Line(pos1.center[0], pos1.center[1], pos2.center[0], pos2.center[1]);
            line.setStroke(color);
            line.setStrokeWidth(screenWidth * 0.01);
            playGround.getChildren().add(line);
        }

        public boolean clikedOnMe(double mouseX, double mouseY){

            double dxl = pos2.center[0] - pos1.center[0];
            double dyl = pos2.center[1] - pos1.center[1];

            double dxc = mouseX - pos1.center[0];
            double dyc = mouseY - pos1.center[1];

            double cross = dxc * dyl - dyc * dxl;

            double ab = Math.sqrt(dxl * dxl + dyl * dyl);
            double h = cross / ab;

            if (Math.abs(h) > screenWidth * 0.01 ){
                return false;
            }

            if (Math.abs(dxl) >= Math.abs(dyl)){
                return dxl > 0 ?
                        pos1.center[0] <= mouseX && mouseX <= pos2.center[0] :
                        pos2.center[0] <= mouseX && mouseX <= pos1.center[0];
            }
            return dyl > 0 ?
                    pos1.center[1] <= mouseY && mouseY <= pos2.center[1] :
                    pos2.center[1] <= mouseY && mouseY <= pos1.center[1];

        }
    }

    public class PlayGround extends Pane {
        public void scaleUpX(double factor){
            screenWidth *= factor;
            gameboardWidth *= factor;
            listOPosition.forEach(pos -> pos.scaleUpX(factor));
            listOfRubberBands.forEach(rubberBand -> rubberBand.scaleUpX(factor));
        }

        public void scaleUpY(double factor){
            screenHeight *= factor;
            gameboardHeight *= factor;
            listOPosition.forEach(pos -> pos.scaleUpY(factor));
            listOfRubberBands.forEach(rubberBand -> rubberBand.scaleUpY(factor));
        }

        public void createBoardTable(int row, int col, Color color){
            for (int i = 0; i < row; i++) {
                for (int j = 0; j < col; j++) {
                    int[] id = new int[]{j, i};
                    listOPosition.add(new OnePosition(id, color));
                }
            }
        }

        public void paint(){
            getChildren().clear();
            Rectangle mainScene = new Rectangle(0, 0, screenWidth, screenHeight);
            mainScene.setFill(Color.YELLOW);
            getChildren().add(mainScene);

            if (listOPosition.isEmpty() || resetGame){
                String startGameText = JOptionPane.showInputDialog("Please write board size: ");

                countOfRow = defaultRow;
                countOfColumn = defaultColumn;

                if (startGameText != null && !startGameText.isEmpty()){
                    String[] text = startGameText.split(" ");
                    int row = Integer.parseInt(text[0]);
                    int column = Integer.parseInt(text[1]);
                    if (text.length == 2 && (row > 1 && row < 8 && column > 1 && column < 8)){
                        countOfRow = row;
                        countOfColumn = column;
                    }
                }

                listOPosition.clear();
                listOfRubberBands.clear();
                resetGame = false;
                createBoardTable(countOfRow, countOfColumn, Color.RED);
                System.out.println("Create rubber Band star game");
                listOfRubberBands.add(new RubberBand(listOfRubberBands.size(), screenWidth * 0.9, screenHeight * 0.2, Color.PURPLE));
                lastClickedRubbenGumID = 0;
                nowUsedRubberBand = listOfRubberBands.get(0);
            }

            changeColor = new Rectangle(screenWidth * 0.8, screenHeight * 0.02, screenWidth * 0.18, screenHeight * 0.07);
            changeColor.setFill(Color.WHITE);
            changeColor.setStroke(Color.BLACK);
            getChildren().add(changeColor);
            Text infoColorText = new Text("Click to change Color");
            infoColorText.setX(changeColor.getX());
            infoColorText.setY(changeColor.getY() + changeColor.getHeight() / 1.5);
            infoColorText.setFont(new Font(screenWidth*0.019));
            infoColorText.setFill(Color.BLACK);
            playGround.getChildren().add(infoColorText);

            Text obvod = new Text("Obvod = " + globalobvod);
            obvod.setX(changeColor.getX());
            obvod.setY(changeColor.getY() + screenHeight * 0.1);
            obvod.setFont(new Font(screenWidth*0.019));
            obvod.setFill(Color.BLACK);
            playGround.getChildren().add(obvod);

            Text obsah = new Text("Osah = " + globalobsah);
            obsah.setX(changeColor.getX());
            obsah.setY(changeColor.getY() + screenHeight * 0.15);
            obsah.setFont(new Font(screenWidth*0.019));
            obsah.setFill(Color.BLACK);
            playGround.getChildren().add(obsah);

            listOPosition.forEach(pos -> {
                pos.update();
                pos.paint();
            });

            listOfRubberBands.forEach(RubberBand::paint);

            if(youLose){
                System.out.println("You loose. (sad)");
                Text condition;
                condition = new Text("Whooopsssss Collision detected :c");
                condition.setX(screenWidth * 0.1);
                condition.setY((double) screenHeight / 2);
                condition.setFont(new Font(72));
                condition.setFill(Color.BLACK);
                condition.setStroke(Color.WHITE);
                condition.setStrokeWidth(2);
                playGround.getChildren().add(condition);

                Text restartGameText;
                restartGameText = new Text("Press R to Restart");
                restartGameText.setX(screenWidth * 0.3);
                restartGameText.setY((double) screenHeight / 2 + 72);
                restartGameText.setFont(new Font(48));
                restartGameText.setFill(Color.BLACK);
                restartGameText.setStroke(Color.WHITE);
                restartGameText.setStrokeWidth(2);
                playGround.getChildren().add(restartGameText);
            }
        }
    }

    public boolean checkPosClick(double mouseX, double mouseY){
        if (nowUsedRubberLine != null){
            return false;
        }

        if (!nowUsedRubberLines.isEmpty()){

            nowUsedRubberBand.update2Lines(nowUsedRubberBand.hidenRubberLines, mouseX, mouseY);
            playGround.paint();
            return true;
        }

        for (OnePosition onePosition : listOPosition){
            Pair<List<RubberLine>, RubberBand> my2values = onePosition.clikedonMe(mouseX, mouseY);

            if (my2values != null){
                System.out.println("click on Blue dot");
                List<RubberLine> clikedRubberLines = my2values.getKey();
                RubberBand clikedRubberBand = my2values.getValue();

                if (nowUsedRubberBand != null && nowUsedRubberBand != clikedRubberBand){
                    return false;
                }

                nowUsedRubberBand = clikedRubberBand;
                nowUsedRubberLines = clikedRubberLines;

                nowUsedRubberBand.hidenRubberLines = new ArrayList<>(clikedRubberLines);
                nowUsedRubberBand.listOfRubberLine.remove(clikedRubberLines.get(0));
                nowUsedRubberBand.listOfRubberLine.remove(clikedRubberLines.get(1));

                nowUsedRubberBand.update2Lines(clikedRubberLines, mouseX, mouseY);

                playGround.paint();
                return true;
            }
        }
        return false;
    }

    public void checkRubberBandClick (double mouseX, double mouseY){
        if (!nowUsedRubberLines.isEmpty()){
            return;
        }

        if (nowUsedRubberBand != null){
            if (nowUsedRubberBand.listOfRubberLine.isEmpty()){
                nowUsedRubberBand.update(mouseX, mouseY);
                playGround.paint();
                return;
            }

            if (nowUsedRubberBand.listOfRubberLine.size() == 1){
                nowUsedRubberBand.updateStart(mouseX, mouseY);
                playGround.paint();
                return;
            }

            nowUsedRubberBand.update2(mouseX, mouseY);
            playGround.paint();
            return;
        }

        for (RubberBand rubberBand : listOfRubberBands){

            if (rubberBand.listOfRubberLine.isEmpty()) {

                if (rubberBand.clikedonMe(mouseX, mouseY)) {
                    System.out.println("listOfRubberLine is Empty for: " + rubberBand);
                    nowUsedRubberBand = rubberBand;
                    System.out.println(rubberBand + " was clicked");
                    System.out.println("call function Update for: " + rubberBand);
                    rubberBand.update(mouseX, mouseY);
                    System.out.println("playground paint");
                    playGround.paint();
                    return;
                }
            }
            else if (rubberBand.listOfRubberLine.size() == 1){
                if (rubberBand.clikedonMe(mouseX, mouseY)){
                    if (nowUsedRubberBand != null && nowUsedRubberBand != rubberBand){
                        return;
                    }
                    System.out.println("listOfRubberLine has one rubberLine for: " + rubberBand);
                    nowUsedRubberBand = rubberBand;
                    nowUsedRubberLine = rubberBand.listOfRubberLine.get(0);
                    System.out.println(rubberBand + " was clicked");
                    System.out.println("call function UpdateStart for: " + rubberBand);
                    rubberBand.updateStart(mouseX, mouseY);
                    System.out.println("playground paint");
                    playGround.paint();
                    return;
                }
            }

            else {
                RubberLine rubberLine = rubberBand.clikedOnMeForlistOfRubberLine(mouseX, mouseY);
                if (rubberLine != null){
                    if (nowUsedRubberBand != null && nowUsedRubberBand != rubberBand){
                        return;
                    }
                    System.out.println("listOfRubberLine has " + rubberBand.listOfRubberLine.size() + " rubberLine for: " + rubberBand);
                    nowUsedRubberBand = rubberBand;
                    nowUsedRubberLine = rubberBand.hidenLine;
                    System.out.println(rubberBand + " was clicked");
                    System.out.println("call function Update2 for: " + rubberBand);
                    rubberBand.update2(mouseX, mouseY);
                    System.out.println("playground paint");
                    playGround.paint();
                    return;
                }
            }
        }
    }

    public void clearItems(){
        nowUsedRubberLines.clear();
        nowUsedRubberBand = null;
        nowUsedRubberLine = null;
        for (RubberBand rubberBand : listOfRubberBands){
            rubberBand.clearMoveObjects();
            rubberBand.hidenRubberLines.clear();
            rubberBand.hidenLine = null;
        }
    }

    public double[] getRadius(OnePosition pos1, OnePosition pos2){
        double radiusX = screenWidth * 0.01;
        double radiusY = screenHeight * 0.01;

        double rX1 = 0, rX2 = 0;
        if (pos1.center[0] < pos2.center[0]){
            rX1 = radiusX;
            rX2 = -radiusX;
        }else if (pos1.center[0] > pos2.center[0]) {
            rX1 = -radiusX;
            rX2 = radiusX;
        }
        double rY1 = 0, rY2 = 0;
        if (pos1.center[1] < pos2.center[1]){
            rY1 = radiusY ;
            rY2 = -radiusY;
        }else if (pos1.center[1] > pos2.center[1]){
            rY1 = -radiusY ;
            rY2 = radiusY ;
        }

        return new double[]{rX1, rY1, rX2, rY2};
    }

    public void testColission(){
        if (nowUsedRubberBand.listOfRubberLine.size() <= 1) {
            return;
        }

        RubberLine rubberLine1 = nowUsedRubberBand.listOfRubberLine.get(nowUsedRubberBand.listOfRubberLine.size() - 2);
        double[] line1R = getRadius(rubberLine1.pos1, rubberLine1.pos2);
        Line2D line1 = new Line2D.Double(rubberLine1.pos1.center[0] + line1R[0], rubberLine1.pos1.center[1] + line1R[1],
                rubberLine1.pos2.center[0] + line1R[2], rubberLine1.pos2.center[1] + line1R[3]);

        RubberLine rubberLine2 = nowUsedRubberBand.listOfRubberLine.get(nowUsedRubberBand.listOfRubberLine.size() - 1);
        double[] line2R = getRadius(rubberLine2.pos1, rubberLine2.pos2);
        Line2D line2 = new Line2D.Double(rubberLine2.pos1.center[0] + line2R[0], rubberLine2.pos1.center[1] + line2R[1],
                rubberLine2.pos2.center[0] + line2R[2], rubberLine2.pos2.center[1] + line2R[3]);

        for (int i = 0; i < nowUsedRubberBand.listOfRubberLine.size()-2; i++) {
            RubberLine testRubberLine = nowUsedRubberBand.listOfRubberLine.get(i);
            double[] testlineR = getRadius(testRubberLine.pos1, testRubberLine.pos2);
            Line2D testLine = new Line2D.Double(testRubberLine.pos1.center[0] + testlineR[0], testRubberLine.pos1.center[1] + testlineR[1],
                    testRubberLine.pos2.center[0] + testlineR[2], testRubberLine.pos2.center[1] + testlineR[3]);

            if(testLine.intersectsLine(line1) || testLine.intersectsLine(line2)){
                youLose = true;
                return;
            }
        }
    }

    public void checkMouseRelease(){
        if (nowUsedRubberBand == null) {
            return;
        }

        if (nowUsedRubberBand.inPos()){
            testColission();

            System.out.println("Rubber Band find postion");
            System.out.println("Obvod " + nowUsedRubberBand.id + ": " + nowUsedRubberBand.getObvod());
            System.out.println("Obsah " + nowUsedRubberBand.id + ": " + nowUsedRubberBand.getObsah());
            globalobvod = nowUsedRubberBand.getObvod();
            globalobsah = nowUsedRubberBand.getObsah();
            if (listOfRubberBands.size() < 4 && nowUsedRubberBand.listOfRubberLine.size() == 1){
                System.out.println("Create new RubberBand");
                Color[] colors = new Color[]{Color.GREEN, Color.WHITE, Color.BLUE};
                listOfRubberBands.add(new RubberBand(listOfRubberBands.size(), screenWidth * 0.95, screenHeight / 2, colors[listOfRubberBands.size()-1]));
            }
        }
        else {
            System.out.println("Rubber Band didn`t find postion. Everething return back");
            if (nowUsedRubberBand.hidenLine != null){
                nowUsedRubberBand.listOfRubberLine.add(nowUsedRubberBand.hidenLine);
            }

            if (nowUsedRubberBand.hidenRubberLines != null && !nowUsedRubberBand.hidenRubberLines.isEmpty()){
                nowUsedRubberBand.listOfRubberLine.add(nowUsedRubberBand.hidenRubberLines.get(0));
                nowUsedRubberBand.listOfRubberLine.add(nowUsedRubberBand.hidenRubberLines.get(1));
            }
        }

        clearItems();
        playGround.paint();
    }

    @Override
    public void start(Stage stage)  {
        try {
            System.out.println("Program start");
            playGround = new PlayGround();
            Scene scene = new Scene(playGround, screenWidth, screenHeight);


            scene.widthProperty().addListener(((observableValue, old, newSceneWidth) -> {
                playGround.scaleUpX((double) newSceneWidth/(double) old);
                playGround.prefWidth((double) newSceneWidth);
                playGround.paint();
            }));

            scene.heightProperty().addListener(((observableValue, old, newSceneHeight) -> {
                playGround.scaleUpY((double) newSceneHeight/(double) old);
                playGround.prefHeight((double) newSceneHeight);
                playGround.paint();
            }));

            scene.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.R && !somethingInput){
                    resetGame = true;
                    youLose = false;
                    playGround.paint();
                }
            });

            scene.setOnMouseClicked(e -> {
                if (e.getX() > changeColor.getX() && changeColor.getX() + changeColor.getWidth() > e.getX()
                    && e.getY() > changeColor.getY() && changeColor.getY() + changeColor.getHeight() > e.getY()){
                    somethingInput = true;
                    String color = JOptionPane.showInputDialog("Please write color: ");
                    somethingInput = false;
                    System.out.println("you write " + color);

                    RubberBand rubberBand = listOfRubberBands.get(lastClickedRubbenGumID);
                    rubberBand.changeColor(color);
                    playGround.paint();

                }
            });

            scene.setOnMouseDragged(e -> {
                if (!youLose) {
                    if (!checkPosClick(e.getX(), e.getY())) {
                        checkRubberBandClick(e.getX(), e.getY());
                    }
                }
            });

            scene.setOnMouseReleased(e -> {
                if (nowUsedRubberBand != null){
                    lastClickedRubbenGumID = nowUsedRubberBand.id;
                }

                checkMouseRelease();
            });

            playGround.paint();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
