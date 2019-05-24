package org.camunda.latera.bss.connectors

import org.camunda.bpm.engine.delegate.DelegateExecution
import javax.mail.*
import javax.mail.internet.*
import javax.activation.*
import javax.mail.Message

class MailSender {

  String     host
  Integer    port
  String     user
  private String     password
  private Session    session
  private Message    message
  private Multipart  multipart
  private Properties props

  MailSender(DelegateExecution execution) {
    def ENV       = System.getenv()

    this.host     = ENV['SMTP_HOST']     ?: execution.getVariable('smtpHost')
    this.port     = (ENV['SMTP_PORT']    ?: execution.getVariable('smtpPort') ?: '587').toInteger()
    this.user     = ENV['SMTP_USER']     ?: execution.getVariable('smtpUser')
    this.password = ENV['SMTP_PASSWORD'] ?: execution.getVariable('smtpPassword')

    this.props = System.getProperties()
    this.props.put("mail.smtp.auth",           "true")
    this.props.put("mail.smtp.starttls.enable", true)
    this.props.put("mail.smtp.ssl.trust",       true)
    this.props.put("mail.smtp.host",            host)
    this.props.put("mail.smtp.port",            port)
    this.props.put("mail.smtp.user",            user)
    this.props.put("mail.smtp.password",        password)

    this.session   = Session.getDefaultInstance(props, null)
    this.message   = new MimeMessage(session)
    this.multipart = new MimeMultipart()
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
