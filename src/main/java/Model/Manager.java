package Model;

import Listeners.SystemEventListener;

import java.io.*;
import java.util.*;

/**
 * The manager class communicates directly with the Program class.
 * It manages a questions list and includes the functionality needed to maintain a questions repository and generate exams.
 */
public class Manager implements Serializable {

    private List<Question> questions;
    private Exam currentExam;
    private List<SystemEventListener> listeners;

    public Manager() {
        questions = new ArrayList();
        listeners = new ArrayList();

    }

    public void fireDisplayQuestions() {
        for (SystemEventListener l : listeners) {
            l.displayQuestionToView(this.toString());
        }
    }

    public int getSize() {
        return questions.size();
    }


    /**
     * @param serial number of the question.
     * @return question matching the passed serial number.
     */
    public Question getQuestionById(int serial) {
        for (Question question : questions) {
            if (question.getSerial() == serial) {
                return question;
            }
        }
        return null;
    }

    public Question getQuestionByIndex(int index) {

        return questions.get(index);
    }

    public MultiChoiceQuestion getMultiChoiceQuestion(int serial) {
        if (isMultiChoiceQuestion(serial)) {
            return (MultiChoiceQuestion) getQuestionById(serial);
        }
        return null;
    }

    public boolean isMultiChoiceQuestion(int serial) {
        if (getQuestionById(serial) == null) {
            fireInvalidQuestionNumberEvent();
            return false;
        }
        if (getQuestionById(serial) instanceof MultiChoiceQuestion) {
            fireIsMultiChoiceEvent();
            return true;
        }
        fireIsOpenQuestionEvent();
        return false;
    }

    public boolean isMultiChoiceQuestionExam(int serial) {
        if (getQuestionById(serial) == null) {
            return false;
        }
        if (getQuestionById(serial) instanceof MultiChoiceQuestion) {
            return true;
        }
        return false;
    }


    private void fireInvalidQuestionNumberEvent() {
        for (SystemEventListener l : listeners) {
            l.invalidQuestionNumberToView();
        }
    }

    private void fireIsOpenQuestionEvent() {
        for (SystemEventListener l : listeners) {
            l.checkIfMultiChoiceQuestionToView(false);
        }
    }


    private void fireIsMultiChoiceEvent() {
        for (SystemEventListener l : listeners) {
            l.checkIfMultiChoiceQuestionToView(true);
        }
    }


    /**
     * @param serial number of the question.
     * @param text   the updated text.
     */
    public void updateQuestion(int serial, String text) {
        if (getQuestionById(serial) == null) {
            fireInvalidQuestionNumberEvent();
            return;
        }
        if (questionTextExists(text)) {
            fireQuestionAlreadyExistsEvent();
            return;
        }
        getQuestionById(serial).setQuestionText(text);
        fireUpdatedQuestionEvent();

    }

    private void fireUpdatedQuestionEvent() {
        for (SystemEventListener l : listeners) {
            l.updateQuestionToView();
        }
    }


    public void addOpenQuestion(String questionText, String answerText) {
        for (Question value : questions) {
            if (value instanceof OpenQuestion && value.getQuestionText().equalsIgnoreCase(questionText)) {
                fireQuestionAlreadyExistsEvent();
                return;
            }
        }
        OpenQuestion question = new OpenQuestion(questionText, answerText);
        questions.add(question);
        fireAddQuestionEvent();

    }


    public boolean addMultiQuestion(String questionText) {
        for (Question value : questions) {
            if (value instanceof MultiChoiceQuestion && value.getQuestionText().equalsIgnoreCase(questionText)) {
                fireQuestionAlreadyExistsEvent();
                return false;
            }
        }
        MultiChoiceQuestion question = new MultiChoiceQuestion(questionText);
        questions.add(question);
        fireAddQuestionEvent();
        return true;

    }

    private void fireAddQuestionEvent() {
        for (SystemEventListener l : listeners) {
            l.addQuestionToView();
            l.updateNumOfQuestionsToView(getSize());
        }
    }

    public void fireUpdateNumOfAnswersEvent(int serial) {
        if (getQuestionById(serial) instanceof OpenQuestion) {
            return;
        }
        for (SystemEventListener l : listeners) {
            l.updateNumOfAnswersToView(getNumOfAnswers(serial));
        }
    }

    private void fireQuestionAlreadyExistsEvent() {
        for (SystemEventListener l : listeners) {
            l.questionAlreadyExistsToView();
        }
    }


    public boolean questionTextExists(String text) {
        for (Question question : questions) {
            if (question instanceof MultiChoiceQuestion q) {
                if (q.getQuestionText().equalsIgnoreCase(text))
                    return true;

            } else if (question.getQuestionText().equalsIgnoreCase(text)) {
                return true;

            }
        }
        return false;

    }

    /**
     * Sorts the questions array in lexicographic order using the insertion sort algorithm.
     */
    public void lexicographicSort(Question[] exam) {
        for (int i = 1; i < exam.length; i++) {
            for (int j = i; j > 0 && exam[j - 1].getQuestionText().compareToIgnoreCase(exam[j].getQuestionText()) > 0; j--) {
                if (exam[j - 1].getQuestionText().compareToIgnoreCase(exam[j].getQuestionText()) > 0) {
                    swap(exam, j - 1, j);

                }
            }
        }

    }


    private void swap(Question[] questions, int i, int j) {
        Question temp = questions[i];
        questions[i] = questions[j];
        questions[j] = temp;
    }


    /**
     * Selects a random question.
     *
     * @return a question at a random index in the questions array.
     */
    public Question selectRandomQuestion() {
        int range = this.getSize();
        int randomIndex = (int) (Math.random() * (range));
        return this.getQuestionByIndex(randomIndex);
    }

    /**
     * Selects a random answer.
     *
     * @param q the question from which an answer will be drawn.
     * @return an answer at a random index in the answers array.
     */

    public MultiChoiceAnswer selectRandomAnswer(MultiChoiceQuestion q) {
        int range = q.getLogicalSize();
        int randomIndex = (int) (Math.random() * (range));
        return q.getAnswerByIndex(randomIndex);

    }


    public boolean containsQuestion(List<Question> qa, Question q) {
        if (qa.size() == 0) {
            return false;
        }
        for (int i = 0; i < qa.size(); i++) {
            if (qa.get(i) != null && qa.get(i).getQuestionText().equals(q.getQuestionText())) {
                return true;
            }

        }
        return false;
    }

    /**
     * @param numOfQuestions the number of questions in the exam.
     * @param repository     the repository from which the random questions are chosen.
     * @return the generated exam.
     */
    public void generateExam(int numOfQuestions, Manager repository) throws FileNotFoundException {
        Comparator<Question> c = new CompareByAnswerLength();
        currentExam = new Exam();

        for (int i = 0; i < numOfQuestions; i++) {
            Question q = repository.selectRandomQuestion();
            while (containsQuestion(currentExam.getQuestions(), q)) {
                q = repository.selectRandomQuestion();
            }
            if (q instanceof OpenQuestion) {

                currentExam.add(new OpenQuestion((OpenQuestion) q));
            } else {

                MultiChoiceQuestion question = (MultiChoiceQuestion) q;
                // variable to store the number of random answers to pick
                int numOfIterations, numOfTrueAnswers = 0;
                for (int j = 0; j < question.getLogicalSize(); j++) {
                    if (question.getAnswerByIndex(j).getTrueFalse()) {
                        numOfTrueAnswers++;
                    }
                }
                numOfIterations = question.getLogicalSize() - numOfTrueAnswers + 1;

                if (numOfIterations > 4) {
                    numOfIterations = 4;
                }
                boolean autoExam = true;
                currentExam.add(new MultiChoiceQuestion(question, autoExam));
                numOfTrueAnswers = 0;
                for (int j = 0; j < numOfIterations; j++) {
                    MultiChoiceAnswer answer = (repository.selectRandomAnswer(question));
                    while (((MultiChoiceQuestion) currentExam.get(i)).answerExists(answer.getAnswer()) || ((numOfTrueAnswers == 1) && answer.getTrueFalse())) {
                        answer = (repository.selectRandomAnswer(question));
                    }
                    if (answer.getTrueFalse()) {
                        numOfTrueAnswers++;
                    }

                    ((MultiChoiceQuestion) currentExam.get(i)).addAnswer(answer.getAnswer(), answer.getTrueFalse());

                }
                ((MultiChoiceQuestion) currentExam.get(i)).answerIndication();

            }

        }

        Collections.sort(currentExam.getQuestions(), c);
        currentExam.loadExamQuestionsToFile();
        currentExam.loadExamSolutionsToFile();
        fireGenerateExamEvent();

    }

    private void fireGenerateExamEvent() {
        for (SystemEventListener l : listeners) {
            l.generateAutomaticExamToView(currentExam.toString());
        }
    }


    public void createExam(List<Integer> examQuestionsId, int numOfQuestions) {
        this.currentExam = new Exam();
        for (int i = 0; i < numOfQuestions; i++) {
            int id = examQuestionsId.get(i);
            if (getQuestionById(id) instanceof MultiChoiceQuestion) {
                boolean autoExam = false;
                MultiChoiceQuestion originalQuestion = (MultiChoiceQuestion) getQuestionById(id);
                this.currentExam.add(new MultiChoiceQuestion(originalQuestion, autoExam));
                for (int j = 0; j < (originalQuestion.getLogicalSize()); j++) {
                    ((MultiChoiceQuestion) this.currentExam.get(i)).addAnswer(originalQuestion.getAnswerByIndex(j).getAnswer(), originalQuestion.getAnswerByIndex(j).getTrueFalse());
                }
            } else if (getQuestionById(id) instanceof OpenQuestion) {
                OpenQuestion originalQuestion = (OpenQuestion) getQuestionById(id);
                this.currentExam.add(new OpenQuestion(originalQuestion));
            }
        }

    }

    public void pickAnswersExam(int serial, List<Integer> answersByIndex) {
        for (int k = 0; k < this.currentExam.getSize(); k++) {
            if (this.currentExam.get(k).getSerial() == serial) {
                boolean answerExist;
                for (int i = 0; i < ((MultiChoiceQuestion) this.currentExam.get(k)).getLogicalSize(); i++) {
                    answerExist = false;
                    for (int j = 0; j < answersByIndex.size() && !answerExist; j++) {
                        if (((MultiChoiceQuestion) this.currentExam.get(k)).getAnswerByIndex(i).getAnswerId() == (answersByIndex.get(j))) {
                            answerExist = true;
                        }
                    }
                    if (!answerExist) {
                        ((MultiChoiceQuestion) this.currentExam.get(k)).deleteAnswer(i);
                        i--;
                    }
                }
                ((MultiChoiceQuestion) this.currentExam.get(k)).answerIndication();
            }
        }
    }

    public void printManualExam() throws FileNotFoundException {
        Comparator<Question> c = new CompareByAnswerLength();
        Collections.sort(currentExam.getQuestions(), c);
        currentExam.loadExamQuestionsToFile();
        currentExam.loadExamSolutionsToFile();
        firePrintManualExam(currentExam.getQuestions().toString());
    }

    private void firePrintManualExam(String exam) {
        for (SystemEventListener l : listeners) {
            l.generateManualExamToView(exam);
        }
    }


    public void loadToBinaryFile() throws IOException {
        ObjectOutputStream outFile = new ObjectOutputStream(new FileOutputStream("questions.dat"));
        outFile.writeObject(questions);
        outFile.close();

    }


    public void updateStaticSerialNumber() {
        Question.setStaticSerial(this.getSize());


    }

    public void loadFromBinaryFile() throws IOException, ClassNotFoundException {
        ObjectInputStream inFile = new ObjectInputStream(new FileInputStream("questions.dat"));
        this.questions = (List<Question>) inFile.readObject();
        inFile.close();
        updateStaticSerialNumber();
        for (SystemEventListener l : listeners) {
            l.updateStartNumOfQuestionsToView(getSize());
        }
    }


    @Override
    public String toString() {
        if (questions.size() == 0) {
            return "There are no questions in the repository.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("\n" + "There are " + questions.size() + " questions: \n");
        for (int i = 0; i < questions.size(); i++) {
            sb.append(questions.get(i).toString() + "\n");
            sb.append("\n");

        }
        return sb.toString();
    }

    /**
     * @param o the object compared to each one of the questions in the repository.
     * @return a boolean indicating whether the passed object equals to one of the answers in the repository.
     */

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Question)) {
            return false;
        }
        for (Question question : this.questions) {
            if (question.getQuestionText().equals(((Question) o).getQuestionText())) {
                return true;
            }
        }
        return false;
    }

    public boolean addAnswer(String text, boolean indicator) {
        return ((MultiChoiceQuestion) questions.get(getSize() - 1)).addAnswer(text, indicator);


    }


    public void updateOpenQuestionAnswer(int serial, String text) {
        ((OpenQuestion) getQuestionById(serial)).setAnswer(text);
        FireUpdateOpenQuestionAnswerEvent();
    }

    private void FireUpdateOpenQuestionAnswerEvent() {
        for (SystemEventListener l : listeners) {
            l.updateOpenQuestionAnswerToView();
        }
    }

    public boolean updateMultiChoiceAnswer(int serial, int answerNum, String text) {
        if (answerNum < 1 || answerNum > getNumOfAnswers(serial)) {
            fireInvalidAnswerNumberEvent();
            return false;
        }
        ((MultiChoiceQuestion) getQuestionById(serial)).getAnswerById(answerNum).setAnswer(text);
        fireUpdatedMultiChoiceAnswerEvent();
        return true;
    }

    private void fireUpdatedMultiChoiceAnswerEvent() {
        for (SystemEventListener l : listeners) {
            l.updateMultiChoiceAnswerToView();
        }
    }

    private void fireInvalidAnswerNumberEvent() {
        for (SystemEventListener l : listeners) {
            l.invalidAnswerNumberToView();
        }
    }

    public void deleteAnswer(int serial, int answerNum) {

        if (getNumOfAnswers(serial) <= 2) {
            fireCantDeleteAnswerEvent();
            return;
        }

        ((MultiChoiceQuestion) getQuestionById(serial)).deleteAnswer(answerNum - 1);
        fireUpdateNumOfAnswersEvent(serial);
        fireDeleteAnswerEvent();

    }

    private void fireCantDeleteAnswerEvent() {
        for (SystemEventListener l : listeners) {
            l.cantDeleteAnswerToView();
        }
    }

    private void fireDeleteAnswerEvent() {
        for (SystemEventListener l : listeners) {
            l.deleteAnswerToView();
        }
    }

    public int getNumOfAnswers(int serial) {
        return ((MultiChoiceQuestion) getQuestionById(serial)).getLogicalSize();

    }

    public void cloneExam() throws CloneNotSupportedException {
        if (this.currentExam == null) {
            return;
        }
        Exam copyExam = this.currentExam.clone();
        fireCopyLastExam(copyExam);
    }

    private void fireCopyLastExam(Exam copyExam) {
        for (SystemEventListener l : listeners) {
            l.copiedExamToView(copyExam.toString());
        }
    }


    public void registerListeners(SystemEventListener listener) {
        this.listeners.add(listener);
    }
}
