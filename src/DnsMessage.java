import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Maxime
 * Date: 31/10/14
 * Time: 16:08
 * To change this template use File | Settings | File Templates.
 */
public class DnsMessage {


    private final short flags;
    private final short transactionId;
    private final short questionsCount;
    private final short answersCount;
    private final short authoritiesCount;
    private final short additionalCount;
    private final ArrayList<Question> questions;
    private final ArrayList<Answer> answers;

    public ArrayList<Question> getQuestions() {
        return questions;
    }

    public ArrayList<Answer> getAnswers() {
        return answers;
    }

    public DnsMessage(DataInputStream stream) throws IOException {
        transactionId = stream.readShort();
        flags = stream.readShort();

        questionsCount = stream.readShort();
        answersCount = stream.readShort();
        authoritiesCount = stream.readShort();
        additionalCount = stream.readShort();

        questions = new ArrayList<Question>();
        answers = new ArrayList<Answer>();

        for(int i = 0; i < questionsCount; i++) {
           questions.add(new Question(stream));
        }

        for(int i = 0; i < answersCount; i++) {
            answers.add(new Answer(stream));
        }
    }

    public boolean isResponse() {
        return flags < 0;
    }

    public boolean isRequest() {
        return !(flags < 0);
    }



    private String parseVariableString(DataInputStream stream) throws IOException {
        String temp = "";
        byte cb;


        while((cb = stream.readByte()) != 0x00){
            byte length = cb;
            for(byte i = 0; i < length; i++){
                cb = stream.readByte();
                temp += (char)cb;
            }
            temp += ".";
        }


        return temp.substring(0,temp.length()-1);
    }



    public short getTransactionId() {
        return transactionId;
    }


    class Question {


        String getName() {
            return name;
        }

        short getType() {
            return type;
        }

        short getKlass() {
            return klass;
        }

        private final String name;
        private final short type;
        private final short klass;

        public Question(DataInputStream stream) throws IOException {

            name = parseVariableString(stream);
            type = stream.readShort();
            klass = stream.readShort();

        }

    }

    class Answer {
        private final int[] rdData;


        private final int name;
        private final int type;
        private final int klass;
        private final int ttl;
        private final int rdLength;

        public Answer(DataInputStream stream) throws IOException {
            name = stream.readUnsignedShort();
            type = stream.readUnsignedShort();
            klass = stream.readUnsignedShort();
            ttl = stream.readInt();
            rdLength = stream.readUnsignedShort();

            rdData = new int[rdLength];
            for(int i = 0; i < rdLength; i++) {
                rdData[i] = stream.readUnsignedByte();
            }
        }

        public String getAddress(){
            assert(rdData.length == 4);
            return String.format("%d.%d.%d.%d", rdData[0], rdData[1], rdData[2], rdData[3]);
        }


    }


    private void toHex(byte b){
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%02X ", b));
        System.out.print(sb.toString() + " ");
    }


}
