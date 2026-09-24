package com.liverpool.liverhack.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Envío de correos con dos transportes:
 *  - Brevo (API HTTPS)  -> se usa cuando existe la variable BREVO_API_KEY (Railway).
 *  - SMTP / Gmail       -> fallback para correr en local.
 *
 * Railway bloquea SMTP (puertos 25/465/587) en los planes Free, Trial y Hobby,
 * por eso en producción hay que salir por HTTPS.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final ObjectProvider<JavaMailSender> mailSender;
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Value("${brevo.api-key:}")
    private String brevoApiKey;

    @Value("${brevo.api-url:https://api.brevo.com/v3/smtp/email}")
    private String brevoApiUrl;

    /** Remitente. Debe estar verificado en Brevo (Settings > Senders). */
    @Value("${mail.from:}")
    private String from;

    @Value("${mail.from-name:Liverpool Talento}")
    private String fromName;

    @Value("${app.base-url:https://liverhack-production.up.railway.app}")
    private String baseUrl;

    public EmailService(ObjectProvider<JavaMailSender> mailSender) {
        this.mailSender = mailSender;
    }

    // ------------------------------------------------------------------
    // Correos de negocio (mismos textos que antes)
    // ------------------------------------------------------------------

    @Async
    public void enviarCorreoEntrevista(String destinatario, String nombre, String fechaHora) {
        String fechaAmigable = fechaHora.replace("T", " a las ");
        String texto = "¡Hola " + nombre + "!\n\nHas avanzado en nuestro proceso de selección.\n"
                + "Atracción de Talento ha programado tu Entrevista para la siguiente fecha:\n👉 "
                + fechaAmigable + " hrs.\n\nInicia sesión en tu Portal de Talento para confirmar tu asistencia:\n"
                + baseUrl + "\n\nAtentamente,\nEquipo de Atracción de Talento Liverpool";
        enviar(destinatario, "Liverpool Talento - Entrevista por Competencias", texto);
    }

    @Async
    public void enviarCorreoEntrevistaHM(String destinatario, String nombre, String fechaHora) {
        String fechaAmigable = fechaHora.replace("T", " a las ");
        String texto = "¡Felicidades " + nombre + "!\n\nHas llegado a la etapa final de nuestro proceso de selección.\n"
                + "Se ha programado tu Entrevista Final con el Hiring Manager para la siguiente fecha:\n👉 "
                + fechaAmigable + " hrs.\n\nInicia sesión en tu Portal de Talento para confirmar tu asistencia:\n"
                + baseUrl + "\n\nAtentamente,\nEquipo de Atracción de Talento Liverpool";
        enviar(destinatario, "Liverpool Talento - Entrevista con Hiring Manager", texto);
    }

    @Async
    public void enviarCorreoOferta(String destinatario, String nombre) {
        String texto = "¡Felicidades " + nombre + "!\n\nTenemos excelentes noticias. Entra a tu Portal de Talento "
                + "para revisar tu Carta Oferta y las condiciones laborales propuestas.\n\n"
                + baseUrl + "\n\nAtentamente,\nEquipo de Atracción de Talento Liverpool";
        enviar(destinatario, "Liverpool Talento - ¡Tenemos una Oferta para ti!", texto);
    }

    @Async
    public void enviarCorreoBienvenida(String destinatario, String nombre, String fechaIngreso) {
        String texto = "¡Bienvenido a la familia Liverpool, " + nombre + "!\n\nTu oferta ha sido procesada exitosamente "
                + "y tu fecha oficial de ingreso será el: " + fechaIngreso + ".\n\nNos vemos pronto para tu onboarding."
                + "\n\nAtentamente,\nEquipo de Atracción de Talento Liverpool";
        enviar(destinatario, "Liverpool Talento - Confirmación de Ingreso", texto);
    }

    // ------------------------------------------------------------------
    // Transporte
    // ------------------------------------------------------------------

    private void enviar(String destinatario, String asunto, String texto) {
        try {
            if (brevoApiKey != null && !brevoApiKey.isBlank()) {
                enviarPorBrevo(destinatario, asunto, texto);
            } else {
                enviarPorSmtp(destinatario, asunto, texto);
            }
        } catch (Exception e) {
            log.error("Error enviando correo '{}' a {}: {}", asunto, destinatario, e.toString(), e);
        }
    }

    private void enviarPorBrevo(String destinatario, String asunto, String texto) throws Exception {
        if (from == null || from.isBlank()) {
            throw new IllegalStateException("Falta la variable MAIL_FROM (remitente verificado en Brevo)");
        }
        String json = "{"
                + "\"sender\":{\"name\":" + q(fromName) + ",\"email\":" + q(from) + "},"
                + "\"to\":[{\"email\":" + q(destinatario) + "}],"
                + "\"subject\":" + q(asunto) + ","
                + "\"textContent\":" + q(texto)
                + "}";

        HttpRequest req = HttpRequest.newBuilder(URI.create(brevoApiUrl))
                .timeout(Duration.ofSeconds(15))
                .header("accept", "application/json")
                .header("content-type", "application/json")
                .header("api-key", brevoApiKey)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() / 100 == 2) {
            log.info("Correo '{}' enviado a {} vía Brevo API ({})", asunto, destinatario, res.statusCode());
        } else {
            log.error("Brevo rechazó el correo a {}: HTTP {} - {}", destinatario, res.statusCode(), res.body());
        }
    }

    private void enviarPorSmtp(String destinatario, String asunto, String texto) {
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            throw new IllegalStateException("No hay JavaMailSender configurado (spring.mail.*)");
        }
        SimpleMailMessage message = new SimpleMailMessage();
        if (from != null && !from.isBlank()) {
            message.setFrom(from);
        }
        message.setTo(destinatario);
        message.setSubject(asunto);
        message.setText(texto);
        sender.send(message);
        log.info("Correo '{}' enviado a {} vía SMTP", asunto, destinatario);
    }

    /** Escapa un String como literal JSON (sin depender de Jackson). */
    private static String q(String s) {
        StringBuilder sb = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"'  -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }
        return sb.append('"').toString();
    }
}
