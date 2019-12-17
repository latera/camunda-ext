package org.camunda.latera.bss.connectors

import org.camunda.bpm.engine.delegate.DelegateExecution
import javax.activation.DataHandler
import javax.mail.Message
import javax.mail.Message.RecipientType
import javax.mail.Multipart
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart
import javax.mail.internet.MimeUtility
import static org.camunda.latera.bss.utils.ListUtil.firstNotNull

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

    this.host     =  execution.getVariable('smtpHost')     ?: ENV['SMTP_HOST']
    this.port     = (execution.getVariable('smtpPort')     ?: ENV['SMTP_PORT'] ?: 587).toInteger()
    this.user     =  execution.getVariable('smtpUser')     ?: ENV['SMTP_USER']
    this.password =  execution.getVariable('smtpPassword') ?: ENV['SMTP_PASSWORD']

    Boolean auth = Boolean.valueOf(firstNotNull([
      execution.getVariable('smtpAuth'),
      ENV['SMTP_AUTH'],
      true
    ]))

    Boolean tls = Boolean.valueOf(firstNotNull([
      execution.getVariable('smtpTLS'),
      ENV['SMTP_TLS'],
      true
    ]))

    Boolean ssl = Boolean.valueOf(firstNotNull([
      execution.getVariable('smtpSSL'),
      ENV['SMTP_SSL'],
      true
    ]))

    this.props = System.getProperties()
    this.props.put('mail.smtp.host',     host)
    this.props.put('mail.smtp.port',     port)
    this.props.put('mail.smtp.user',     user)
    this.props.put('mail.smtp.password', password)
    this.props.put('mail.smtp.auth',     auth)
    this.props.put('mail.mime.encodefilename', true)

    if (tls) {
      this.props.put('mail.smtp.starttls.enable', tls.toString()) // DO NOT REMOVE toString() here !!!
    }
    if (ssl) {
      this.props.put('mail.smtp.ssl.trust', ssl)
    } else if (tls) {
      this.props.put('mail.smtp.ssl.trust', host)
    }

    this.session   = Session.getDefaultInstance(props, null)
    this.message   = new MimeMessage(session)
    this.multipart = new MimeMultipart()
    this.setFrom(user)
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

  MailSender attachFile(CharSequence name, CharSequence urlStr = '') {
    MimeBodyPart part = new MimeBodyPart()
    URL url = new URL(urlStr)
    part.setDataHandler(new DataHandler(url))
    part.setFileName(MimeUtility.encodeText(name, 'UTF-8', null))
    multipart.addBodyPart(part)
    return this
  }

  void send(){
    Transport transport = session.getTransport('smtp')
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
