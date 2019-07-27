package org.camunda.latera.bss.connectors

import org.camunda.bpm.engine.delegate.DelegateExecution
import javax.mail.*
import javax.mail.internet.*
import javax.activation.*
import javax.mail.Message

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
    def ENV       = System.getenv()

    this.host     = ENV['SMTP_HOST']     ?: execution.getVariable('smtpHost')
    this.port     = (ENV['SMTP_PORT']    ?: execution.getVariable('smtpPort') ?: '587').toInteger()
    this.user     = ENV['SMTP_USER']     ?: execution.getVariable('smtpUser')
    this.password = ENV['SMTP_PASSWORD'] ?: execution.getVariable('smtpPassword')

    this.props = System.getProperties()
    this.props.put('mail.smtp.host',            host)
    this.props.put('mail.smtp.port',            port)
    this.props.put('mail.smtp.user',            user)
    this.props.put('mail.smtp.password',        password)
    this.props.put('mail.smtp.auth',            true)
    this.props.put('mail.smtp.starttls.enable', true)
    this.props.put('mail.smtp.ssl.trust',       true)

    this.session   = Session.getDefaultInstance(props, null)
    this.message   = new MimeMessage(session)
    this.multipart = new MimeMultipart()
  }

  MailSender getHost() {
    return this.host
  }

  MailSender setHost(CharSequence host) {
    this.host = host
    this.props.put("mail.smtp.host", host)
    return this
  }

  MailSender getPort() {
    return this.port
  }

  MailSender setPort(Integer port) {
    this.port = port
    this.props.put("mail.smtp.port", port)
    return this
  }

  MailSender setPort(CharSequence port) {
    return setPort(port.toInteger())
  }

  MailSender getUser() {
    return this.user
  }

  MailSender setUser(CharSequence user) {
    this.user = user
    this.props.put("mail.smtp.user", user)
    return this
  }

  MailSender setPassword(CharSequence password) {
    this.password = password
    this.props.put("mail.smtp.password", password)
    return this
  }

  MailSender setFrom(CharSequence from) {
    message.setFrom(new InternetAddress(from.toString()))
    return this
  }

  MailSender addRecipient(CharSequence recipient, Message.RecipientType type = Message.RecipientType.TO) {
    message.addRecipient(type, new InternetAddress(recipient.toString()))
    return this
  }

  MailSender setSubject(CharSequence subject) {
    message.setSubject(subject.toString())
    return this
  }

  MailSender addTextPart(CharSequence body) {
    MimeBodyPart part = new MimeBodyPart()
    part.setText(body.toString())
    multipart.addBodyPart(part)
    return this
  }

  MailSender addFile(CharSequence filename, Object datasource){
    MimeBodyPart part = new MimeBodyPart()
    part.setDataHandler(new DataHandler(datasource, 'application/octet-stream'))
    part.setFileName(filename.toString())
    multipart.addBodyPart(part)
    return this
  }

  void send(){
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
