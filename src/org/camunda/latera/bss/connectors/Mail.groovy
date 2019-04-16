package org.camunda.latera.bss.connectors

import org.camunda.bpm.engine.delegate.DelegateExecution
import javax.mail.*
import javax.mail.Message
import javax.mail.internet.*
import javax.activation.*

class MailSender {

  private String     host
  private Integer    port
  private String     user
  private String     password
  private Session    session
  private Message    message
  private Multipart  multipart
  private Properties props

  MailSender(DelegateExecution execution) {
    host     = execution.getVariable('smtpHost')
    port     = execution.getVariable('smtpPort') ?: 587
    user     = execution.getVariable('smtpUser')
    password = execution.getVariable('smtpPassword')

    props = System.getProperties()
    props.put("mail.smtp.auth",           "true")
    props.put("mail.smtp.starttls.enable", true)
    props.put("mail.smtp.ssl.trust",       true)
    props.put("mail.smtp.host",            host)
    props.put("mail.smtp.port",            port)
    props.put("mail.smtp.user",            user)
    props.put("mail.smtp.password",        password)

    session   = Session.getDefaultInstance(props, null)
    message   = new MimeMessage(session)
    multipart = new MimeMultipart()
  }

  def setFrom(String from) {
    message.setFrom(new InternetAddress(from))
  }

  def addRecipient(String recipient, Message.RecipientType type = Message.RecipientType.TO) {
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
    transport.connect(host, port, user, password)
    try {
      message.setContent(multipart)
      transport.sendMessage(message, message.getAllRecipients())
    }
    finally {
      transport.close()
    }
  }
}
