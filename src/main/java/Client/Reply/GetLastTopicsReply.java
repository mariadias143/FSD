package Client.Reply;

import java.util.List;

public class GetLastTopicsReply extends Reply {
    private List<String> msgs;
    private boolean status;
    private int error_code;


    public GetLastTopicsReply(List<String> msgs){
        this.status = true;
        this.error_code = 0;
        this.msgs = msgs;
    }

    public GetLastTopicsReply(int error){
        this.status = false;
        this.error_code = error;
    }

    @Override
    public void printContent() {
        if (status){
            StringBuilder st = new StringBuilder();
            st.append("Get last topics in subscribed topics \n");
            for(String mgs : this.msgs){
                st.append(mgs + "\n");
            }

            System.out.println(st.toString());
        }
        else{
            System.out.println(error());
        }
    }

    @Override
    public String error() {
        String res = "";

        switch (this.error_code){
            case 0:
                res = "Ta tudo bem";
                break;
            case 1:
                res = "Erro nas credenciais";
                break;
            default:
                res = "Outro erro";
                break;
        }

        return res;
    }
}
