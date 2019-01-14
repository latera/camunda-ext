package org.camunda.latera.bss.connectors

import org.camunda.bpm.engine.delegate.DelegateExecution
import javax.mail.*
import javax.mail.internet.*
import javax.activation.*

class MailSender {

  private String smtp_host
  private Integer smtp_port
  private String smtp_user
  private String smtp_password
  private Session session
  private Message message
  private Multipart multipart
  private Properties props

  MailSender(DelegateExecution execution) {
    smtp_host = execution.getVariable('smtp_host')
    smtp_port = execution.getVariable('smtp_port')?execution.getVariable('smtp_port').toInteger():587
    smtp_user = execution.getVariable('smtp_user')
    smtp_password = execution.getVariable('smtp_password')

    props = System.getProperties()

    props.put("mail.smtp.auth", "true")
    props.put("mail.smtp.starttls.enable", true)
    props.put("mail.smtp.ssl.trust", true)
    props.put("mail.smtp.host", smtp_host)
    props.put("mail.smtp.port", smtp_port)
    props.put("mail.smtp.user", smtp_user)
    props.put("mail.smtp.password", smtp_password)

    session = Session.getDefaultInstance(props, null)
    message = new MimeMessage(session)
    multipart = new MimeMultipart()
  }

  def setFrom(String from) {
    message.setFrom(new InternetAddress(from))
  }

  def addRecipient(String recipient, Message.RecipientType type=Message.RecipientType.TO) {
    message.addRecipient(type, new InternetAddress(recipient))
  }

  def setSubject(String subject) {
    message.setSubject(subject)
  }

  def addTextPart(String body) {
    MimeBodyPart part = new MimeBodyPart()
    part.setText(body)
    multipart.addBodyPart(part)
  }

  def addFile(String filename, Object datasource){
    MimeBodyPart part = new MimeBodyPart()
    part.setDataHandler(new DataHandler(datasource, 'application/octet-stream'))
    part.setFileName(filename)
    multipart.addBodyPart(part)
  }

  def send(){
    Transport transport = session.getTransport("smtp")
    transport.connect(smtp_host, smtp_port, smtp_user, smtp_password)
    try {
      message.setContent(multipart)
      transport.sendMessage(message, message.getAllRecipients())
    }
    finally {
      transport.close()
    }
  }
}
