package com.liverpool.liverhack.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void enviarCorreoEntrevista(String destinatario, String nombre, String fechaHora) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("liverpoolhackatlon@gmail.com");
            message.setTo(destinatario);
            message.setSubject("Liverpool Talento - Entrevista por Competencias");
            String fechaAmigable = fechaHora.replace("T", " a las ");
            String texto = "¡Hola " + nombre + "!\n\nHas avanzado en nuestro proceso de selección.\nAtracción de Talento ha programado tu Entrevista para la siguiente fecha:\n👉 " + fechaAmigable + " hrs.\n\nInicia sesión en tu Portal de Talento para confirmar tu asistencia:\nhttp://localhost:8082/login\n\nAtentamente,\nEquipo de Atracción de Talento Liverpool";
            message.setText(texto);
            mailSender.send(message);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void enviarCorreoEntrevistaHM(String destinatario, String nombre, String fechaHora) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("liverpoolhackatlon@gmail.com");
            message.setTo(destinatario);
            message.setSubject("Liverpool Talento - Entrevista con Hiring Manager");
            String fechaAmigable = fechaHora.replace("T", " a las ");
            String texto = "¡Felicidades " + nombre + "!\n\nHas llegado a la etapa final de nuestro proceso de selección.\nSe ha programado tu Entrevista Final con el Hiring Manager para la siguiente fecha:\n👉 " + fechaAmigable + " hrs.\n\nInicia sesión en tu Portal de Talento para confirmar tu asistencia:\nhttp://localhost:8082/login\n\nAtentamente,\nEquipo de Atracción de Talento Liverpool";
            message.setText(texto);
            mailSender.send(message);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void enviarCorreoOferta(String destinatario, String nombre) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("liverpoolhackatlon@gmail.com");
            message.setTo(destinatario);
            message.setSubject("Liverpool Talento - ¡Tenemos una Oferta para ti!");
            String texto = "¡Felicidades " + nombre + "!\n\nTenemos excelentes noticias. Entra a tu Portal de Talento para revisar tu Carta Oferta y las condiciones laborales propuestas.\n\nhttp://localhost:8082/login\n\nAtentamente,\nEquipo de Atracción de Talento Liverpool";
            message.setText(texto);
            mailSender.send(message);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void enviarCorreoBienvenida(String destinatario, String nombre, String fechaIngreso) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("liverpoolhackatlon@gmail.com");
            message.setTo(destinatario);
            message.setSubject("Liverpool Talento - Confirmación de Ingreso");
            String texto = "¡Bienvenido a la familia Liverpool, " + nombre + "!\n\nTu oferta ha sido procesada exitosamente y tu fecha oficial de ingreso será el: " + fechaIngreso + ".\n\nNos vemos pronto para tu onboarding.\n\nAtentamente,\nEquipo de Atracción de Talento Liverpool";
            message.setText(texto);
            mailSender.send(message);
        } catch (Exception e) { e.printStackTrace(); }
    }

    // Notificaciones de cambio de turno
    public void enviarAvisoAHM(String correoHM, int cantidad) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom("liverpoolhackatlon@gmail.com");
            msg.setTo(correoHM);
            msg.setSubject("Liverpool Talento - Tienes candidatos por validar");
            msg.setText("Hola Hiring Manager,\n\nAtracción de Talento ha terminado su filtro y tienes " + cantidad + " candidato(s) esperando tu validación.\nPor favor ingresa al portal:\nhttp://localhost:8082/login");
            mailSender.send(msg);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void enviarAvisoAAT(String correoAT, String candidato, String fase) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom("liverpoolhackatlon@gmail.com");
            msg.setTo(correoAT);
            msg.setSubject("Liverpool Talento - HM ha tomado una decisión");
            msg.setText("Hola Atracción de Talento,\n\nEl Hiring Manager ha evaluado a '" + candidato + "' en la fase de " + fase + ".\nYa puedes continuar con el proceso en el portal:\nhttp://localhost:8082/login");
            mailSender.send(msg);
        } catch (Exception e) { e.printStackTrace(); }
    }

    // Alertas de retraso
    public void alertaRetrasoHM(String correoHM, String correoHRBP, String candidato) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom("liverpoolhackatlon@gmail.com");
            msg.setTo(correoHM);
            if (correoHRBP != null) msg.setCc(correoHRBP);
            msg.setSubject("URGENTE: Retraso en Validación - Candidato: " + candidato);
            msg.setText("Hola Hiring Manager,\n\nEl proceso de '" + candidato + "' está detenido en tu bandeja. Por favor, emite tu decisión a la brevedad para no afectar el SLA.\n\n*Copia de seguimiento para HRBP.");
            mailSender.send(msg);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void alertaRetrasoAT(String correoAT, String correoHRBP, String candidato) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom("liverpoolhackatlon@gmail.com");
            msg.setTo(correoAT);
            if (correoHRBP != null) msg.setCc(correoHRBP);
            msg.setSubject("URGENTE: Retraso en Atracción de Talento - " + candidato);
            msg.setText("Hola Atracción de Talento,\n\nEl Hiring Manager ha reportado un retraso en el seguimiento del candidato '" + candidato + "'. Por favor atiende este caso a la brevedad.\n\n*Copia de seguimiento para HRBP.");
            mailSender.send(msg);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void alertaRetrasoAT_DesdeHRBP(String correoAT, String candidato) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom("liverpoolhackatlon@gmail.com");
            msg.setTo(correoAT);
            msg.setSubject("SEGUIMIENTO HRBP: Retraso de Carta Oferta - " + candidato);
            msg.setText("Hola Atracción de Talento,\n\nComo HRBP he notado que la oferta para '" + candidato + "' lleva tiempo sin ser enviada o confirmada. Por favor actualiza el estatus en el portal.");
            mailSender.send(msg);
        } catch (Exception e) { e.printStackTrace(); }
    }
}