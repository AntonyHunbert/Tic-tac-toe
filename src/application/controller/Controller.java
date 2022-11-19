package application.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.application.Platform;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class Controller implements Initializable {
    private Socket socket;
    private InputStream is;
    private OutputStream os;
//    private String board = "000000000";

    private int currentPos = -1;
    private static final int PLAY_1 = 1;
    private static final int PLAY_2 = 2;
    private static final int EMPTY = 0;
    private static final int BOUND = 90;
    private static final int OFFSET = 15;

    @FXML
    private Pane base_square;

    private Text notice;

    @FXML
    private Rectangle game_panel;

    private static boolean TURN = false;

    private static final int[][] chessBoard = new int[3][3];
    private static final boolean[][] flag = new boolean[3][3];

    @Override
    public void initialize(URL location, ResourceBundle resources){
        int port = 8081;
        try {
            socket = new Socket("localhost", port);
            System.out.println("ip and port: " + socket.getRemoteSocketAddress());
            os = socket.getOutputStream();
            is = socket.getInputStream();

            notice = new Text();
            base_square.getChildren().add(notice);
            showNotice("welcome, " + socket.getLocalPort());

        } catch (IOException e) {
            e.printStackTrace();
        }

        new Thread(() -> {
            Scanner sc = new Scanner(is);
            PrintWriter pw = new PrintWriter(os);
            String fromServer = "";
            String toServer = "";
            while (sc.hasNextLine()){
                fromServer = sc.nextLine();
                System.out.println("from server: " + fromServer);
                String commandCode = fromServer.split("@")[0];
                String commandContent = fromServer.split("@")[1];
                System.out.println(commandContent);
                if(commandCode.equals("1")){
//            游戏进行中
                    if (commandContent.equals("9")){
//                开局，开始落子,落子结束后将currentPos传回服务器
                        Platform.runLater(() -> showNotice("game start, choose a position and place your chess:"));
//                        System.out.println("game start, choose a position and put your chess:");
                        game_panel.setOnMouseClicked(event -> {
                            int x = (int) (event.getX() / BOUND);
                            int y = (int) (event.getY() / BOUND);
                            System.out.println(x + ",|" + y);
                            currentPos = x * 3 + y;
                            System.out.println(currentPos);

                            if (refreshBoard(x, y)) {
                                TURN = !TURN;
                                System.out.println("start judging...");
                                System.out.println(Arrays.deepToString(chessBoard));
                                int isEnd = judgeEnd();
                                if (isEnd == 1){
                                    Platform.runLater(() -> showNotice("congratulations! you are the winner!!"));
                                } else if (isEnd == 2) {
                                    Platform.runLater(() -> showNotice("draw!!!"));
                                }
                                else {
                                    Platform.runLater(() -> showNotice("opponent's turn, wait for a few seconds.."));
                                }
                                System.out.println(isEnd);
                                pw.println(isEnd * 10 + currentPos);
                                pw.flush();
                                System.out.println("back to server: " + (isEnd * 10 + currentPos));
                            }

                        });
//                        落子后判断当前棋局，返回值 = isEnd * 10 + currentPos



                    }
                    else {
//                局中，先根据对方的消息更新棋盘，并判断是否结束，若未结束，开始我的回合
                        int i = Integer.parseInt(commandContent) / 3;
                        int j = Integer.parseInt(commandContent) % 3;
                        Platform.runLater(() -> {
                            if (refreshBoard(i, j)) {
                                TURN = !TURN;
                            }
                        });

                        Platform.runLater(() -> showNotice("your turn, choose a position and place your chess:"));
                        game_panel.setOnMouseClicked(event -> {
                            int x = (int) (event.getX() / BOUND);
                            int y = (int) (event.getY() / BOUND);
                            System.out.println(x + ",|" + y);
                            currentPos = x * 3 + y;

                            if (refreshBoard(x, y)) {
                                TURN = !TURN;
                                System.out.println("start judging...");
                                System.out.println(Arrays.deepToString(chessBoard));
                                int isEnd = judgeEnd();
                                if (isEnd == 1){
                                    Platform.runLater(() -> showNotice("congratulations! you are the winner!!"));
                                } else if (isEnd == 2) {
                                    Platform.runLater(() -> showNotice("draw!!!"));
                                }
                                else {
                                    Platform.runLater(() -> showNotice("opponent's turn, wait for a few seconds.."));
                                }
                                System.out.println(isEnd);
                                pw.println(isEnd * 10 + currentPos);
                                pw.flush();
                                System.out.println("back to server" + (isEnd * 10 + currentPos));
                            }

                        });






//                更新棋盘后需要判断输/赢/平局
//                        System.out.println("start judging...");
//                        System.out.println(Arrays.deepToString(chessBoard));
//                        int isEnd = judgeEnd();
//                        System.out.println(isEnd);

//                        if (isEnd == 1){
////                    游戏结束，对方胜利
//                            toServer = "11";
//                            System.out.println(toServer);
//                            pw.println(toServer);
//                            pw.flush();
//                        }
//                        else if (isEnd == 2){
////                    游戏结束，平局
//                            toServer = "10";
//                            pw.println(toServer);
//                            pw.flush();
//                        }
//                        else {
//                            System.out.println("my turn, choose a position and put your chess:");
//                            game_panel.setOnMouseClicked(event -> {
//                                int x = (int) (event.getX() / BOUND);
//                                int y = (int) (event.getY() / BOUND);
//                                System.out.println(x + ",|" + y);
//                                currentPos = x * 3 + y;
//                                System.out.println(currentPos);
//                                pw.println(currentPos);
//                                pw.flush();
//                                System.out.println("back to server" + currentPos);
//
//
//                                if (refreshBoard(x, y)) {
//                                    TURN = !TURN;
//                                }
//
//                            });
//
//                        }
                    }
                }
                else{
                    if (commandContent.equals("welcome!")){
//                进入房间，打开棋盘，返回我准备好了
                        pw.println("9");
                        pw.flush();

                        System.out.println("open chessboard...");
                    }
//            游戏结束，
                    else if(commandCode.equals("2")){
//                        更新局面，输了
                        int i = Integer.parseInt(commandContent) / 3;
                        int j = Integer.parseInt(commandContent) % 3;
                        Platform.runLater(() -> {
                            if (refreshBoard(i, j)) {
                                TURN = !TURN;
                            }
                        });
                        Platform.runLater(() -> showNotice("sorry, you lost the game..."));
                    } else if (commandCode.equals("3")) {
                        int i = Integer.parseInt(commandContent) / 3;
                        int j = Integer.parseInt(commandContent) % 3;
                        Platform.runLater(() -> {
                            if (refreshBoard(i, j)) {
                                TURN = !TURN;
                            }
                        });
                        Platform.runLater(() -> showNotice("draw!!!"));
                    }

                }



            }
        }).start();


//        game_panel.setOnMouseClicked(event -> {
//            int x = (int) (event.getX() / BOUND);
//            int y = (int) (event.getY() / BOUND);
//            System.out.println(x + "," + y);
//            if (refreshBoard(x, y)) {
//                TURN = !TURN;
//            }
//        });


//接收消息，判断是否开始游戏

//        if (sc.hasNextLine()){
//            fromServer = sc.nextLine();
//            System.out.println(fromServer);
//        }



        System.out.println("end of init...");

    }

    private void showNotice(String n){
        notice.setText(n);
    }

    private boolean refreshBoard (int x, int y) {
        if (!flag[x][y]) {
            chessBoard[x][y] = TURN ? PLAY_1 : PLAY_2;
            drawChess();
            return true;
        }
        return false;
    }

    private int judgeEnd(){
//        终局共9种情况，其中一种为平
//        0: 未到终局  1: 我方胜利  2: 平局
        for(int i = 0; i < chessBoard.length; i++){
            int chessSum = chessBoard[i][0] + chessBoard[i][1] + chessBoard[i][2];
            if(flag[i][0] && flag[i][1] && flag[i][2] && (chessSum == 3 || chessSum == 6)){
                return 1;
            }
        }
        for(int j = 0; j < chessBoard.length; j++){
            int chessSum = chessBoard[0][j] + chessBoard[1][j] + chessBoard[2][j];
            if(flag[0][j] && flag[1][j] && flag[2][j] && (chessSum == 3 || chessSum == 6)){
                return 1;
            }
        }
        int chessSum = chessBoard[0][0] + chessBoard[1][1] + chessBoard[2][2];
        if (flag[0][0] && flag[1][1] && flag[2][2] && (chessSum == 3 || chessSum == 6)){
            return 1;
        }
        chessSum = chessBoard[0][2] + chessBoard[1][1] + chessBoard[2][0];
        if (flag[0][2] && flag[1][1] && flag[2][0] && (chessSum == 3 || chessSum == 6)){
            return 1;
        }
//        平局判定
        boolean isFull = true;
        for(int i = 0; i < chessBoard.length; i++){
            for(int j = 0; j < chessBoard.length; j++){
                isFull = isFull & flag[i][j];
            }
        }
        if (isFull){
            return 2;
        }
        return 0;
    }

//

    private void drawChess () {
        for (int i = 0; i < chessBoard.length; i++) {
            for (int j = 0; j < chessBoard[0].length; j++) {
                if (flag[i][j]) {
                    // This square has been drawing, ignore.
                    continue;
                }
                switch (chessBoard[i][j]) {
                    case PLAY_1:
                        drawCircle(i, j);
                        break;
                    case PLAY_2:
                        drawLine(i, j);
                        break;
                    case EMPTY:
                        // do nothing
                        break;
                    default:
                        System.err.println("Invalid value!");
                }
            }
        }
    }

    private void drawCircle (int i, int j) {
        Circle circle = new Circle();
        base_square.getChildren().add(circle);
        circle.setCenterX(i * BOUND + BOUND / 2.0 + OFFSET);
        circle.setCenterY(j * BOUND + BOUND / 2.0 + OFFSET);
        circle.setRadius(BOUND / 2.0 - OFFSET / 2.0);
        circle.setStroke(Color.RED);
        circle.setFill(Color.TRANSPARENT);
        flag[i][j] = true;
    }

    private void drawLine (int i, int j) {
        Line line_a = new Line();
        Line line_b = new Line();
        base_square.getChildren().add(line_a);
        base_square.getChildren().add(line_b);
        line_a.setStartX(i * BOUND + OFFSET * 1.5);
        line_a.setStartY(j * BOUND + OFFSET * 1.5);
        line_a.setEndX((i + 1) * BOUND + OFFSET * 0.5);
        line_a.setEndY((j + 1) * BOUND + OFFSET * 0.5);
        line_a.setStroke(Color.BLUE);

        line_b.setStartX((i + 1) * BOUND + OFFSET * 0.5);
        line_b.setStartY(j * BOUND + OFFSET * 1.5);
        line_b.setEndX(i * BOUND + OFFSET * 1.5);
        line_b.setEndY((j + 1) * BOUND + OFFSET * 0.5);
        line_b.setStroke(Color.BLUE);
        flag[i][j] = true;
    }
}
