/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package apretastebrowser;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.Authenticator;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.JOptionPane;

/**
 *
 * @author Victor
 */
public class JMailer {
    private String host, mailStoreType, userName, password;
    Properties properties;
    Session emailSession;
    Store store;
    
    public JMailer(String host, String mailStoreType, String userName, String password){
        this.host = host;
        this.mailStoreType = mailStoreType;
        this.userName = userName;
        this.password = password;
        properties = new Properties();
        properties.put("mail.store.protocol", mailStoreType);
        properties.put("mail.pop3.host", host);
        properties.put("mail.pop3.port", "995");
        properties.put("mail.pop3.starttls.enable", "true");
        Session emailSession = Session.getDefaultInstance(properties);
        try{
            store = emailSession.getStore("pop3s");
        }catch(Exception e){
            JOptionPane.showConfirmDialog(null, e.toString());
        }
        
    }
    
    public Message[] fetch() throws MessagingException{
        
        store.connect(host, userName, password);
        Folder emailFolder = store.getFolder("INBOX");
        emailFolder.open(Folder.READ_ONLY);

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

         // retrieve the messages from the folder in an array and print it
        Message[] messages = emailFolder.getMessages();
        return messages;
    }
    
    public void sendRequest(String req){
        Properties props = System.getProperties();
        props = new Properties();
            props.put("mail.smtp.user", "apretasteclient@gmail.com");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.starttls.enable","true");
            props.put("mail.smtp.debug", "true");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.socketFactory.port", "587");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.fallback", "false");

        // Get the default Session object.
        Session session = Session.getInstance(props, new SMTPAuthenticator());

        try{
           // Create a default MimeMessage object.
           MimeMessage message = new MimeMessage(session);

           // Set From: header field of the header.
           message.setFrom(new InternetAddress(userName));

           // Set To: header field of the header.
           message.addRecipient(Message.RecipientType.TO, new InternetAddress("apretaste@gmail.com"));

           // Set Subject: header field
           message.setSubject(req);

           // Now set the actual message
           message.setText("This is actual message");

           // Send message
           Transport transport = session.getTransport("smtps");
           transport.connect("smtp.gmail.com", 465, userName, password);
           transport.sendMessage(message, message.getAllRecipients());
           transport.close();  

           System.out.println("Sent message successfully....");
        }catch (MessagingException mex) {
           mex.printStackTrace();
        }
    }
    
    public static void main(String[] args){
        JMailer jm = new JMailer("pop.gmail.com", "pop3", "apretasteclient@gmail.com", "1234567890poiuytrewq");
        try{
            Message[] ms = jm.fetch();
            
            /*for (Message m : ms){
                String msgBody = jm.writePart(m);
                System.out.println(msgBody);
                
                System.out.println("+++++++++++++++++++++++++++++++++++++++++");
                //jm.writePart(m);
            }*/
            InputStream is = ms[0].getInputStream();
            OutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[2048];
            for (int n = 0; n >= 0; n = is.read(buffer))
                out.write(buffer, 0, n);
            
            String[] rawEmail = out.toString().split("\r\n");
                    
            String html = "";
            boolean found = false;
            for (String x : rawEmail){
                if (found || x.indexOf("<html") == 0){
                    System.out.println(x);    
                    html = html.concat(x);
                    found = true;
                }
                if (x.contains("</html>"))
                    break;
            }
            //System.out.println(html);
            
        }
        catch(Exception e){
            
        }
        //jm.sendRequest("wikipedia jose marti");
    }
    
    public String writePart(Part p) throws Exception {
        String ret = "";
      //check if the content is plain text
      if (p.isMimeType("text/plain") ) {
         System.out.println("This is plain text");
         //System.out.println("---------------------------");
         ret += ((String) p.getContent());
      } 
      else if (p.isMimeType("text/html")){
          System.out.println("This is html part");
          ret += p.toString();
      }
      //check if the content has attachment
      else if (p.isMimeType("multipart/*")) {
         //System.out.println("This is a Multipart");
         //System.out.println("---------------------------");
         Multipart mp = (Multipart) p.getContent();
         int count = mp.getCount();
         for (int i = 0; i < count; i++)
            ret += writePart(mp.getBodyPart(i));
      } 
      //check if the content is a nested message
      else if (p.isMimeType("message/rfc822")) {
         //System.out.println("This is a Nested Message");
         //System.out.println("---------------------------");
         ret += writePart((Part) p.getContent());
      } 
      //check if the content is an inline image
      else if (p.isMimeType("image/jpeg")) {
         //System.out.println("--------> image/jpeg");
         Object o = p.getContent();

         InputStream x = (InputStream) o;
         // Construct the required byte array
         
         int i = 0;
         byte[] bArray = new byte[x.available()];
         //System.out.println("x.length = " + x.available());
         while ((i = (int) ((InputStream) x).available()) > 0) {
            int result = (int) (((InputStream) x).read(bArray));
            if (result == -1)
                i=0;
            break;
         }
         FileOutputStream f2 = new FileOutputStream("/tmp/image.jpg");
         f2.write(bArray);
      } 
      else if (p.getContentType().contains("image/")) {
         //System.out.println("content type" + p.getContentType());
         File f = new File("image" + new Date().getTime() + ".jpg");
         DataOutputStream output = new DataOutputStream(
            new BufferedOutputStream(new FileOutputStream(f)));
            com.sun.mail.util.BASE64DecoderStream test = 
                 (com.sun.mail.util.BASE64DecoderStream) p
                  .getContent();
         byte[] buffer = new byte[1024];
         int bytesRead;
         while ((bytesRead = test.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
         }
      } 
      else {
         Object o = p.getContent();
         if (o instanceof String) {
            //System.out.println("This is a string");
            //System.out.println("---------------------------");
            //System.out.println((String) o);
            ret += (String) o;
         } 
         else if (o instanceof InputStream) {
            //System.out.println("This is just an input stream");
            //System.out.println("---------------------------");
            InputStream is = (InputStream) o;
            is = (InputStream) o;
            int c;
            while ((c = is.read()) != -1){
               //System.out.write(c);
                ret+=c;
            }
         } 
         else {
            //System.out.println("This is an unknown type");
            //System.out.println("---------------------------");
            //System.out.println(o.toString());
             ret += o.toString();
         }
      }
      return ret;
   }
    
    
    
    /*public void writePart(Part p) throws Exception {

      //check if the content is plain text
      if (p.isMimeType("text/plain")) {
         System.out.println("This is plain text----------");
         System.out.println("---------------------------");
         System.out.println((String) p.getContent());
      } 
      //check if the content has attachment
      else if (p.isMimeType("multipart/*")) {
         System.out.println("This is a Multipart");
         System.out.println("---------------------------");
         Multipart mp = (Multipart) p.getContent();
         int count = mp.getCount();
         for (int i = 0; i < count; i++)
            writePart(mp.getBodyPart(i));
      } 
      //check if the content is a nested message
      else if (p.isMimeType("message/rfc822")) {
         System.out.println("This is a Nested Message");
         System.out.println("---------------------------");
         writePart((Part) p.getContent());
      } 
      //check if the content is an inline image
      else if (p.isMimeType("image/jpeg")) {
         System.out.println("--------> image/jpeg");
         Object o = p.getContent();

         InputStream x = (InputStream) o;
         // Construct the required byte array
         int i = 0;
         byte[] bArray = new byte[x.available()];
         System.out.println("x.length = " + x.available());
         while ((i = (int) ((InputStream) x).available()) > 0) {
            int result = (int) (((InputStream) x).read(bArray));
            if (result == -1)

            break;
         }
         FileOutputStream f2 = new FileOutputStream("/tmp/image.jpg");
         f2.write(bArray);
      } 
      else if (p.getContentType().contains("image/")) {
         System.out.println("content type" + p.getContentType());
         File f = new File("image" + new Date().getTime() + ".jpg");
         DataOutputStream output = new DataOutputStream(
            new BufferedOutputStream(new FileOutputStream(f)));
            com.sun.mail.util.BASE64DecoderStream test = 
                 (com.sun.mail.util.BASE64DecoderStream) p
                  .getContent();
         byte[] buffer = new byte[1024];
         int bytesRead;
         while ((bytesRead = test.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
         }
      } 
      else {
         Object o = p.getContent();
         if (o instanceof String) {
            System.out.println("This is a string");
            System.out.println("---------------------------");
            System.out.println((String) o);
         } 
         else if (o instanceof InputStream) {
            System.out.println("This is just an input stream");
            System.out.println("---------------------------");
            InputStream is = (InputStream) o;
            is = (InputStream) o;
            int c;
            while ((c = is.read()) != -1)
               System.out.write(c);
         } 
         else {
            System.out.println("This is an unknown type");
            System.out.println("---------------------------");
            System.out.println(o.toString());
         }
      }

   }*/
    
    private class SMTPAuthenticator extends Authenticator
    {
        public PasswordAuthentication getPasswordAuthentication()
        {
            return new PasswordAuthentication("apretasteclient@gmail.com", "1234567890poiuytrewq");
        }
    }
}
