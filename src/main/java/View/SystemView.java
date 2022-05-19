package View;

import Listeners.SystemUIEventListener;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.swing.*;

import javafx.scene.control.ScrollPane;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;


public class SystemView implements AbstractSystemView {

    private List<SystemUIEventListener> allListeners = new ArrayList<>();
    private Scene menuScene, addQuestionScene, updateQuestionScene, updateAnswerScene;
    private final Stage stgDisplayQuestion = new Stage();

    //  btnReturnToMenu.setTranslateX(100);
    //  btnReturnToMenu.setTranslateY(100);
    public SystemView(Stage theStage) {
        theStage.setTitle("Exam Generator");
        VBox vbMenu = new VBox();

        Button btnDisplayQuestions = new Button("Display Questions");
        btnDisplayQuestions.setMaxWidth(150);
        btnDisplayQuestions.setOnAction(actionEvent -> {
            for (SystemUIEventListener l : allListeners) {
                l.displayQuestionToModel();
            }
        });

        Button btnAddQuestion = new Button("Add Question");
        btnAddQuestion.setMaxWidth(150);
        btnAddQuestion.setOnAction(actionEvent -> theStage.setScene(addQuestionScene));


        Button btnUpdateQuestion = new Button("Update Question");
        btnUpdateQuestion.setMaxWidth(150);
        btnUpdateQuestion.setOnAction(actionEvent -> {
            theStage.setScene(updateQuestionScene);
            for (SystemUIEventListener l : allListeners) {
                l.displayQuestionToModel();
            }
        });
        Button btnUpdateAnswer = new Button("Update Answer");
        btnUpdateAnswer.setMaxWidth(150);



        VBox vbAddQuestion = new VBox();
        HBox hbQuestionText = new HBox();
        hbQuestionText.setSpacing(5);
        Label addQuestionLabel = new Label("Enter question text:");
        TextField tfAddQuestion = new TextField();
        hbQuestionText.getChildren().addAll(addQuestionLabel, tfAddQuestion);
        Label chooseTypeLabel = new Label("Choose type of question:");
        ToggleGroup tglQuestionType = new ToggleGroup();
        RadioButton rdoAmericanButton = new RadioButton("American Question");
        RadioButton rdoOpenQuestionButton = new RadioButton("Open Question");
        rdoAmericanButton.setToggleGroup(tglQuestionType);
        rdoOpenQuestionButton.setToggleGroup(tglQuestionType);
        vbAddQuestion.setPadding(new Insets(10));
        vbAddQuestion.setSpacing(10);

        HBox hbAddOpenAnswer = new HBox();
        hbAddOpenAnswer.setVisible(false);
        Label lblAddOpenAnswer = new Label("Enter answer text:");
        TextField tfAnswerText = new TextField();
        Button btnSubmitOpenAnswer = new Button("Submit question&answer");
        btnSubmitOpenAnswer.setOnAction((actionEvent -> {
            if (tfAddQuestion.getText().equals("") || tfAnswerText.getText().equals("")) {
                JOptionPane.showMessageDialog(null, "Question/answer fields cannot be empty. Try again.");
                return;
            }
            for (SystemUIEventListener l : allListeners) {
                l.addOpenQuestionToModel(tfAddQuestion.getText(), tfAnswerText.getText());
            }
            tfAddQuestion.clear();
            tfAnswerText.clear();
        }));
        hbAddOpenAnswer.setPadding(new Insets(10));
        hbAddOpenAnswer.setSpacing(5);
        hbAddOpenAnswer.getChildren().addAll(lblAddOpenAnswer, tfAnswerText, btnSubmitOpenAnswer);

        VBox vbAddAmericanAnswer = new VBox();
        vbAddAmericanAnswer.setVisible(false);
        LinkedHashSet<String> answersList = new LinkedHashSet<>();
        ArrayList<Boolean> booleanList = new ArrayList<>();
        HBox hbAddAmericanAnswer = new HBox();
        Label lblAddAmericanAnswer = new Label("Enter answers one by one:");
        TextField tfAmericanAnswerText = new TextField();
        ToggleGroup tglBooleanValue = new ToggleGroup();
        RadioButton btnTrue = new RadioButton("True");
        RadioButton btnFalse = new RadioButton("False");
        btnTrue.setToggleGroup(tglBooleanValue);
        btnFalse.setToggleGroup(tglBooleanValue);
        Button btnSubmitAmericanAnswer = new Button("Submit answer");
        btnSubmitAmericanAnswer.setOnAction(actionEvent -> {
            if (tfAmericanAnswerText.getText().equals("")) {
                JOptionPane.showMessageDialog(null, "answer field cannot be empty. Try again.");
                return;
            }
            if (tglBooleanValue.getSelectedToggle() != null && tglBooleanValue.getSelectedToggle().equals(btnTrue)) {
                booleanList.add(true);
            } else if (tglBooleanValue.getSelectedToggle() != null && tglBooleanValue.getSelectedToggle().equals(btnFalse)) {
                booleanList.add(false);
            }
            answersList.add(tfAmericanAnswerText.getText());
            tfAmericanAnswerText.clear();
        });
        Button btnSubmit = new Button("Submit final question&answers");
        btnSubmit.setOnAction(actionEvent -> {
            if (answersList.size() < 2) {
                JOptionPane.showMessageDialog(null, "Minimum 2 answers required.");
                return;
            }
            if (tfAddQuestion.getText().equals("")) {
                JOptionPane.showMessageDialog(null, "Question field cannot be empty. Try again.");
                answersList.clear();
                booleanList.clear();
                tfAddQuestion.clear();
                return;
            }
            for (SystemUIEventListener l : allListeners) {
                l.addMultiChoiceQuestionToModel(tfAddQuestion.getText(), answersList, booleanList);
                answersList.clear();
                booleanList.clear();
                tfAddQuestion.clear();
            }
        });
        hbAddAmericanAnswer.getChildren().addAll(lblAddAmericanAnswer, tfAmericanAnswerText);
        vbAddAmericanAnswer.setSpacing(5);
        vbAddAmericanAnswer.getChildren().addAll(hbAddAmericanAnswer, btnTrue, btnFalse, btnSubmitAmericanAnswer, btnSubmit);
        Button btnReturnToMenu1 = new Button("Return to main menu");
        btnReturnToMenu1.setOnAction(actionEvent -> theStage.setScene(menuScene));
        vbAddQuestion.getChildren().addAll(hbQuestionText, chooseTypeLabel, rdoAmericanButton, rdoOpenQuestionButton, hbAddOpenAnswer, vbAddAmericanAnswer, btnReturnToMenu1);
        addQuestionScene = new Scene(vbAddQuestion, 500, 500);
        rdoAmericanButton.setOnAction(actionEvent -> {
            hbAddOpenAnswer.setVisible(false);
            vbAddAmericanAnswer.setVisible(true);
        });
        rdoOpenQuestionButton.setOnAction(actionEvent -> {
            vbAddAmericanAnswer.setVisible(false);
            hbAddOpenAnswer.setVisible(true);

        });



        VBox vbUpdateQuestion = new VBox();
        Label lblUpdatedQuestionNum = new Label("Enter question number below:");
        TextField tfQuestionNum = new TextField();
        tfQuestionNum.setMaxSize(50, 50);
        Label lblUpdatedQuestionText = new Label("Enter the updated text:");
        TextField tfUpdatedQuestionText = new TextField();
        Button btnSubmitUpdatedQuestion = new Button("Submit updated answer.");
        btnSubmitUpdatedQuestion.setOnAction(actionEvent -> {
            for (SystemUIEventListener l : allListeners) {
                l.updateQuestionToModel(tfQuestionNum.getText(), tfUpdatedQuestionText.getText());
            }
        });
        vbUpdateQuestion.setSpacing(5);
        vbUpdateQuestion.setPadding(new Insets(10));
        Button btnReturnToMenu2 = new Button("Return to main menu");
        btnReturnToMenu2.setOnAction(actionEvent -> theStage.setScene(menuScene));
        vbUpdateQuestion.getChildren().addAll(lblUpdatedQuestionNum, tfQuestionNum, lblUpdatedQuestionText, tfUpdatedQuestionText, btnSubmitUpdatedQuestion, btnReturnToMenu2);
        updateQuestionScene = new Scene(vbUpdateQuestion, 500, 500);



        VBox vbUpdateAnswer = new VBox();
        Label lblQuestionNum = new Label("Enter question number below:");
        TextField tfQuestionNum2 = new TextField();
        tfQuestionNum2.setMaxSize(50, 50);
     


        vbMenu.getChildren().addAll(btnDisplayQuestions, btnAddQuestion, btnUpdateQuestion, btnUpdateAnswer);
        vbMenu.setSpacing(5);
        vbMenu.setPadding(new Insets(10));
        menuScene = new Scene(vbMenu, 500, 500);
        theStage.setScene(menuScene);
        theStage.show();


    }


    @Override
    public void registerListener(SystemUIEventListener listener) {
        allListeners.add(listener);
    }


    @Override
    public void displayQuestions(String s) {
        stgDisplayQuestion.setTitle("Questions list");
        ScrollPane sp = new ScrollPane(new Label(s));
        stgDisplayQuestion.setScene(new Scene(sp, 300, 250));
        stgDisplayQuestion.showAndWait();

    }

    public void showPopUpMessage(String msg) {
        JOptionPane.showMessageDialog(null, msg);


    }


}
