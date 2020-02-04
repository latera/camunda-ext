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
import static org.camunda.latera.bss.utils.StringUtil.isEmpty
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
  private Transport  transport

  MailSender(DelegateExecution execution) {
    def ENV       = System.getenv()

    this.host      =  execution.getVariable('smtpHost')     ?: ENV['SMTP_HOST']
    this.port      = (execution.getVariable('smtpPort')     ?: ENV['SMTP_PORT'] ?: 587).toInteger()
    this.user      =  execution.getVariable('smtpUser')     ?: ENV['SMTP_USER']
    this.password  =  execution.getVariable('smtpPassword') ?: ENV['SMTP_PASSWORD']
    this.transport =  session.getTransport('smtp')

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

    this.session = Session.getDefaultInstance(props, null)
    this.newMessage()
  }

  MailSender connect() {
    if (!this.transport.isConnected()) {
      if (isEmpty(this.host)) {
        throw new Exception("Empty host name!")
      }
      if (isEmpty(this.port)) {
        throw new Exception("Empty port number!")
      }
      if (isEmpty(this.user)) {
        throw new Exception("Empty user name!")
      }
      if (isEmpty(this.password)) {
        throw new Exception("Empty password!")
      }
      this.transport.connect(this.host, this.port, this.user, this.password)
    }
    return this
  }

  String getHost() {
    return this.host
  }

  MailSender setHost(CharSequence host) {
    this.host = host
    this.props.put("mail.smtp.host", host)
    return this
  }

  Integer getPort() {
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

  String getUser() {
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
    this.message.setFrom(new InternetAddress(from.toString()))
    return this
  }

  MailSender newMessage() {
    this.message   = new MimeMessage(session)
    this.multipart = new MimeMultipart()
    setFrom(user)
    return this
  }

  MailSender addRecipient(CharSequence recipient, Message.RecipientType type = Message.RecipientType.TO) {
    this.message.addRecipient(type, new InternetAddress(recipient.toString()))
    return this
  }

  MailSender setSubject(CharSequence subject) {
    this.message.setSubject(subject.toString())
    return this
  }

  MailSender addTextPart(CharSequence body) {
    MimeBodyPart part = new MimeBodyPart()
    part.setText(body.toString())
    this.multipart.addBodyPart(part)
    return this
  }

  MailSender addFile(CharSequence name, Object datasource) {
    MimeBodyPart part = new MimeBodyPart()
    part.setDataHandler(new DataHandler(datasource, 'application/octet-stream'))
    part.setFileName(MimeUtility.encodeText(name, 'UTF-8', null))
    this.multipart.addBodyPart(part)
    return this
  }

  MailSender attachFile(CharSequence name, CharSequence urlStr = '') {
    MimeBodyPart part = new MimeBodyPart()
    URL url = new URL(urlStr)
    part.setDataHandler(new DataHandler(url))
    part.setFileName(MimeUtility.encodeText(name, 'UTF-8', null))
    this.multipart.addBodyPart(part)
    return this
  }

  Boolean send() {
    Boolean result = true
    connect()
    try {
      this.message.setContent(this.multipart)
      this.transport.sendMessage(this.message, this.message.getAllRecipients())
    } catch (Exception e) {
      logger.error(e)
      result = false
    } finally {
      transport.close()
    }

    return result
  }
}
