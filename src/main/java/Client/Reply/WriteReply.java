package Client.Reply;

public class WriteReply extends Reply {
    private boolean status;
    private int error_code;


    public WriteReply(){
        this.status = true;
    }

    public WriteReply(int error_code){
        this.status = false;
        this.error_code = error_code;
    }


    @Override
    public void printContent() {
        if (status){
            System.out.println("Escrita bem sucedida");
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
            case 2:
                res = "Username já utilizado";
                break;
            default:
                res = "Outro erro";
                break;
        }

        return res;
    }
}
