package com.liverpool.liverhack.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Async
    public void enviarCorreoEntrevista(String destinatario, String nombre, String fechaHora) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(destinatario);
            message.setSubject("Liverpool Talento - Entrevista por Competencias");
            String fechaAmigable = fechaHora.replace("T", " a las ");
            String texto = "¡Hola " + nombre + "!\n\nHas avanzado en nuestro proceso de selección.\nAtracción de Talento ha programado tu Entrevista para la siguiente fecha:\n👉 " + fechaAmigable + " hrs.\n\nInicia sesión en tu Portal de Talento para confirmar tu asistencia:\nliverhack-production.up.railway.app\n\nAtentamente,\nEquipo de Atracción de Talento Liverpool";
            message.setText(texto);
            mailSender.send(message);
        } catch (Exception e) { System.out.println("Error enviando correo: " + e.getMessage()); }
    }

    @Async
    public void enviarCorreoEntrevistaHM(String destinatario, String nombre, String fechaHora) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(destinatario);
            message.setSubject("Liverpool Talento - Entrevista con Hiring Manager");
            String fechaAmigable = fechaHora.replace("T", " a las ");
            String texto = "¡Felicidades " + nombre + "!\n\nHas llegado a la etapa final de nuestro proceso de selección.\nSe ha programado tu Entrevista Final con el Hiring Manager para la siguiente fecha:\n👉 " + fechaAmigable + " hrs.\n\nInicia sesión en tu Portal de Talento para confirmar tu asistencia:\nliverhack-production.up.railway.app\n\nAtentamente,\nEquipo de Atracción de Talento Liverpool";
            message.setText(texto);
            mailSender.send(message);
        } catch (Exception e) { System.out.println("Error enviando correo HM: " + e.getMessage()); }
    }

    @Async
    public void enviarCorreoOferta(String destinatario, String nombre) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(destinatario);
            message.setSubject("Liverpool Talento - ¡Tenemos una Oferta para ti!");
            String texto = "¡Felicidades " + nombre + "!\n\nTenemos excelentes noticias. Entra a tu Portal de Talento para revisar tu Carta Oferta y las condiciones laborales propuestas.\n\nliverhack-production.up.railway.app\n\nAtentamente,\nEquipo de Atracción de Talento Liverpool";
            message.setText(texto);
            mailSender.send(message);
        } catch (Exception e) { System.out.println("Error enviando correo Oferta: " + e.getMessage()); }
    }

    @Async
    public void enviarCorreoBienvenida(String destinatario, String nombre, String fechaIngreso) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(destinatario);
            message.setSubject("Liverpool Talento - Confirmación de Ingreso");
            String texto = "¡Bienvenido a la familia Liverpool, " + nombre + "!\n\nTu oferta ha sido procesada exitosamente y tu fecha oficial de ingreso será el: " + fechaIngreso + ".\n\nNos vemos pronto para tu onboarding.\n\nAtentamente,\nEquipo de Atracción de Talento Liverpool";
            message.setText(texto);
            mailSender.send(message);
        } catch (Exception e) { System.out.println("Error enviando correo Ingreso: " + e.getMessage()); }
    }
}