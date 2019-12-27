package utils.Req;

import Client.Request.Request;

import java.util.Collection;

public class PostMessage  extends Request {
        private String mensagem;
        private Collection<String> topicos;
        private String request;

      public PostMessage(String mensagem, Collection<String> topicos){
          this.mensagem=mensagem;
          this.topicos=topicos;
          this.request= "POST/message";
          super.setRequest(this.request);
      }




}
