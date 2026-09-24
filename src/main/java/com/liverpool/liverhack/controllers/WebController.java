package com.liverpool.liverhack.controllers;

import com.liverpool.liverhack.models.Candidato;
import com.liverpool.liverhack.models.Usuario;
import com.liverpool.liverhack.repositories.CandidatoRepository;
import com.liverpool.liverhack.repositories.UsuarioRepository;
import com.liverpool.liverhack.services.EmailService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@Controller
public class WebController {

    @Autowired
    private CandidatoRepository candidatoRepo;

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private EmailService emailService;

    // ==========================================
    // RUTAS DE ACCESO (LOGIN / LOGOUT)
    // ==========================================
    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String mostrarLogin(HttpSession session) {
        if (session.getAttribute("usuarioLogueado") != null) {
            return "redirect:/dashboard";
        }
        return "login";
    }

    @PostMapping("/login")
    public String procesarLogin(@RequestParam String username, @RequestParam String password, HttpSession session, Model model) {

        Usuario user = usuarioRepo.findByUsernameAndPassword(username, password);

        if (user != null) {

            // Si es candidato, verificamos estrictamente que exista su expediente antes de dejarlo pasar
            if ("CANDIDATO".equals(user.getRol())) {
                Candidato candidato = candidatoRepo.findByNombre(user.getNombreCompleto());

                if (candidato != null) {
                    session.setAttribute("usuarioLogueado", user);
                    session.setAttribute("candidatoId", candidato.getId());
                    return "redirect:/dashboard";
                } else {
                    // Si no hay expediente, lo regresamos al login con un mensaje claro
                    model.addAttribute("error", "Error de sistema: No se encontró el expediente de candidato para '" + user.getNombreCompleto() + "'.");
                    return "login";
                }
            }

            // Si es AT, HM o HRBP, pasa directo
            session.setAttribute("usuarioLogueado", user);
            return "redirect:/dashboard";
        }

        model.addAttribute("error", "Credenciales incorrectas. Verifica tu usuario y contraseña.");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    // ==========================================
    // DASHBOARD PRINCIPAL Y ENRUTADOR
    // ==========================================
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) return "redirect:/login";

        model.addAttribute("usuario", usuario);

        if (usuario.getRol().equals("CANDIDATO")) {
            cargarDatosCandidato(session, model);
            return "candidato/dashboard";
        }

        List<Candidato> listaAT = candidatoRepo.findByStatusProceso("Nuevo");
        List<Candidato> listaHM = candidatoRepo.findByStatusProceso("Validacion HM");
        List<Candidato> listaFase3AT = candidatoRepo.findByStatusProceso("En Busqueda");
        List<Candidato> listaFase3HM = candidatoRepo.findByStatusProceso("Revision CV HM");

        List<Candidato> listaFase4AT = new ArrayList<>();
        listaFase4AT.addAll(candidatoRepo.findByStatusProceso("En Atraccion"));
        listaFase4AT.addAll(candidatoRepo.findByStatusProceso("Entrevista Agendada"));
        listaFase4AT.addAll(candidatoRepo.findByStatusProceso("Entrevista Confirmada"));
        List<Candidato> listaFase4HM = candidatoRepo.findByStatusProceso("Revision Pool HM");

        List<Candidato> listaFase5AT = new ArrayList<>();
        listaFase5AT.addAll(candidatoRepo.findByStatusProceso("En Seleccion"));
        listaFase5AT.addAll(candidatoRepo.findByStatusProceso("Entrevista HM Agendada"));
        List<Candidato> listaFase5HM = candidatoRepo.findByStatusProceso("Entrevista HM Confirmada");

        List<Candidato> listaFase6HRBP = candidatoRepo.findByStatusProceso("En Oferta");
        List<Candidato> listaFase6AT = new ArrayList<>();
        listaFase6AT.addAll(candidatoRepo.findByStatusProceso("Carta Oferta Solicitada"));
        listaFase6AT.addAll(candidatoRepo.findByStatusProceso("Oferta Enviada"));
        listaFase6AT.addAll(candidatoRepo.findByStatusProceso("Oferta Firmada"));

        List<Candidato> listaContratados = candidatoRepo.findByStatusProceso("Contratado");

        // ==========================================
        // LA MAGIA DEL TRACKER 100%
        // ==========================================
        int faseGlobal = 1; String turnoActivo = "";

        if (!listaAT.isEmpty() || !listaHM.isEmpty()) { faseGlobal = 2; turnoActivo = (!listaAT.isEmpty()) ? "AT" : "HM"; }
        else if (!listaFase3AT.isEmpty() || !listaFase3HM.isEmpty()) { faseGlobal = 3; turnoActivo = (!listaFase3AT.isEmpty()) ? "AT" : "HM"; }
        else if (!listaFase4AT.isEmpty() || !listaFase4HM.isEmpty()) { faseGlobal = 4; turnoActivo = (!listaFase4AT.isEmpty()) ? "AT" : "HM"; }
        else if (!listaFase5AT.isEmpty() || !listaFase5HM.isEmpty()) { faseGlobal = 5; turnoActivo = (!listaFase5AT.isEmpty()) ? "AT" : "HM"; }
        else if (!listaFase6HRBP.isEmpty() || !listaFase6AT.isEmpty()) { faseGlobal = 6; turnoActivo = (!listaFase6HRBP.isEmpty()) ? "HRBP" : "AT"; }
        else if (!listaContratados.isEmpty()) { faseGlobal = 7; turnoActivo = "COMPLETADO"; } // FASE 7 (100%)

        model.addAttribute("faseGlobal", faseGlobal);
        model.addAttribute("turnoActivo", turnoActivo);

        if (usuario.getRol().equals("AT")) {
            model.addAttribute("candidatosAT", listaAT);
            model.addAttribute("candidatosFase3", listaFase3AT);
            model.addAttribute("candidatosFase4", listaFase4AT);
            model.addAttribute("candidatosFase5", listaFase5AT);
            model.addAttribute("candidatosFase6", listaFase6AT);
            model.addAttribute("contratados", listaContratados);
            return "at/dashboard";
        } else if (usuario.getRol().equals("HM")) {
            model.addAttribute("candidatosHM", listaHM);
            model.addAttribute("candidatosFase3", listaFase3HM);
            model.addAttribute("candidatosFase4", listaFase4HM);
            model.addAttribute("candidatosFase5", listaFase5HM);
            model.addAttribute("contratados", listaContratados);
            return "hm/dashboard";
        } else if (usuario.getRol().equals("HRBP")) {
            // UNIFICAMOS LISTAS PARA LA VISTA ESPÍA DEL HRBP
            List<Candidato> fase2 = new ArrayList<>(listaAT); fase2.addAll(listaHM);
            List<Candidato> fase3 = new ArrayList<>(listaFase3AT); fase3.addAll(listaFase3HM);
            List<Candidato> fase4 = new ArrayList<>(listaFase4AT); fase4.addAll(listaFase4HM);
            List<Candidato> fase5 = new ArrayList<>(listaFase5AT); fase5.addAll(listaFase5HM);

            model.addAttribute("candidatosFase2", fase2);
            model.addAttribute("candidatosFase3", fase3);
            model.addAttribute("candidatosFase4", fase4);
            model.addAttribute("candidatosFase5", fase5);
            model.addAttribute("candidatosFase6HRBP", listaFase6HRBP);
            model.addAttribute("candidatosFase6AT", listaFase6AT);
            model.addAttribute("contratados", listaContratados);
            return "hrbp/dashboard";
        }

        return "redirect:/login";
    }

    private void cargarDatosCandidato(HttpSession session, Model model) {
        Long candId = (Long) session.getAttribute("candidatoId");
        Candidato miPerfil = candidatoRepo.findById(candId).orElse(new Candidato());
        model.addAttribute("miPerfil", miPerfil);

        int progreso = 10; int fase = 1; boolean descartado = false;
        String status = miPerfil.getStatusProceso();

        if (status != null) {
            if (status.equals("Nuevo")) { progreso = 25; fase = 2; }
            else if (status.equals("Validacion HM")) { progreso = 35; fase = 2; }
            else if (status.equals("En Busqueda") || status.equals("Revision CV HM")) { progreso = 50; fase = 3; }
            else if (status.equals("En Atraccion") || status.equals("Entrevista Agendada") || status.equals("Entrevista Confirmada") || status.equals("Revision Pool HM")) { progreso = 70; fase = 4; }
            else if (status.equals("En Seleccion") || status.equals("Entrevista HM Agendada") || status.equals("Entrevista HM Confirmada")) { progreso = 85; fase = 5; }
            else if (status.equals("En Oferta") || status.equals("Carta Oferta Solicitada")) { progreso = 90; fase = 6; }
            else if (status.equals("Oferta Enviada")) { progreso = 95; fase = 6; }
            else if (status.equals("Oferta Firmada") || status.equals("Contratado")) { progreso = 100; fase = 6; }
            else if (status.contains("Descartado")) { progreso = 100; descartado = true; }
        }
        model.addAttribute("progreso", progreso);
        model.addAttribute("fase", fase);
        model.addAttribute("descartado", descartado);
    }

    // ==========================================
    // ENDPOINTS DE TRANSICIÓN (FASES 2 A 5)
    // ==========================================
    @PostMapping("/at/evaluar-lote")
    public String evaluarLoteAT(@RequestParam(value = "candidatoIds", required = false) List<Long> ids) {
        if (ids != null) { List<Candidato> cands = candidatoRepo.findAllById(ids); for (Candidato c : cands) c.setStatusProceso("Validacion HM"); candidatoRepo.saveAll(cands); }
        return "redirect:/dashboard";
    }

    @PostMapping("/at/descartar/{id}")
    public String descartarAT(@PathVariable Long id, @RequestParam String justificacion) {
        Candidato c = candidatoRepo.findById(id).orElseThrow(); c.setStatusProceso("Descartado por AT"); c.setStatusJustificacion(justificacion); candidatoRepo.save(c); return "redirect:/dashboard";
    }

    @PostMapping("/hm/evaluar/{id}")
    public String evaluarHM(@PathVariable Long id, @RequestParam String decision, @RequestParam(required = false) String justificacion) {
        Candidato c = candidatoRepo.findById(id).orElseThrow();
        if ("Aprobado".equals(decision)) c.setStatusProceso("En Busqueda"); else { c.setStatusProceso("Descartado por HM (Fase 2)"); c.setStatusJustificacion(justificacion); }
        candidatoRepo.save(c); return "redirect:/dashboard";
    }

    @PostMapping("/at/fase3-avanzar/{id}")
    public String avanzarFase3AT(@PathVariable Long id) {
        Candidato c = candidatoRepo.findById(id).orElseThrow(); c.setStatusProceso("Revision CV HM"); candidatoRepo.save(c); return "redirect:/dashboard";
    }

    @PostMapping("/hm/fase3-evaluar/{id}")
    public String evaluarFase3HM(@PathVariable Long id, @RequestParam String decision, @RequestParam(required = false) String justificacion) {
        Candidato c = candidatoRepo.findById(id).orElseThrow();
        if ("Aprobado".equals(decision)) c.setStatusProceso("En Atraccion"); else { c.setStatusProceso("Descartado por HM (Fase 3)"); c.setStatusJustificacion(justificacion); }
        candidatoRepo.save(c); return "redirect:/dashboard";
    }

    @PostMapping("/at/fase4-agendar/{id}")
    public String agendarFase4AT(@PathVariable Long id, @RequestParam String fechaEntrevista) {
        Candidato c = candidatoRepo.findById(id).orElseThrow(); c.setFechaEntrevista(fechaEntrevista); c.setStatusProceso("Entrevista Agendada"); candidatoRepo.save(c);
        emailService.enviarCorreoEntrevista(c.getCorreo(), c.getNombre(), fechaEntrevista);
        return "redirect:/dashboard";
    }

    @PostMapping("/candidato/confirmar-entrevista/{id}")
    public String candidatoConfirmarEntrevista(@PathVariable Long id) {
        Candidato c = candidatoRepo.findById(id).orElseThrow(); c.setStatusProceso("Entrevista Confirmada"); candidatoRepo.save(c); return "redirect:/dashboard";
    }

    @PostMapping("/at/fase4-evaluar/{id}")
    public String evaluarFase4AT(@PathVariable Long id, @RequestParam Integer score, @RequestParam String recomendacion, @RequestParam String notas) {
        Candidato c = candidatoRepo.findById(id).orElseThrow(); c.setScorePsicometrico(score); c.setRecomendacionAT(recomendacion); c.setNotasEntrevista(notas); c.setStatusProceso("Revision Pool HM"); candidatoRepo.save(c); return "redirect:/dashboard";
    }

    @PostMapping("/hm/fase4-decidir/{id}")
    public String decidirFase4HM(@PathVariable Long id, @RequestParam String decision, @RequestParam(required = false) String justificacion) {
        Candidato c = candidatoRepo.findById(id).orElseThrow();
        if ("Aprobado".equals(decision)) c.setStatusProceso("En Seleccion"); else { c.setStatusProceso("Descartado por HM (Fase 4)"); c.setStatusJustificacion(justificacion); }
        candidatoRepo.save(c); return "redirect:/dashboard";
    }

    @PostMapping("/at/fase5-agendar/{id}")
    public String agendarFase5AT(@PathVariable Long id, @RequestParam String fechaEntrevista) {
        Candidato c = candidatoRepo.findById(id).orElseThrow(); c.setFechaEntrevista(fechaEntrevista); c.setStatusProceso("Entrevista HM Agendada"); candidatoRepo.save(c);
        emailService.enviarCorreoEntrevistaHM(c.getCorreo(), c.getNombre(), fechaEntrevista);
        return "redirect:/dashboard";
    }

    @PostMapping("/candidato/confirmar-entrevista-hm/{id}")
    public String candidatoConfirmarEntrevistaHM(@PathVariable Long id) {
        Candidato c = candidatoRepo.findById(id).orElseThrow(); c.setStatusProceso("Entrevista HM Confirmada"); candidatoRepo.save(c); return "redirect:/dashboard";
    }

    @PostMapping("/hm/fase5-decidir/{id}")
    public String decidirFase5HM(@PathVariable Long id, @RequestParam String decision, @RequestParam(required = false) String justificacion) {
        Candidato c = candidatoRepo.findById(id).orElseThrow();
        if ("Finalista".equals(decision)) c.setStatusProceso("En Oferta"); else { c.setStatusProceso("Descartado por HM (Fase 5)"); c.setStatusJustificacion(justificacion); }
        candidatoRepo.save(c); return "redirect:/dashboard";
    }

    // ==========================================
    // ENDPOINTS DE LA FASE 6 (NUEVOS)
    // ==========================================
    @PostMapping("/hrbp/fase6-solicitar/{id}")
    public String hrbpSolicitarOferta(@PathVariable Long id) {
        Candidato c = candidatoRepo.findById(id).orElseThrow();
        c.setStatusProceso("Carta Oferta Solicitada");
        candidatoRepo.save(c);
        return "redirect:/dashboard";
    }

    @PostMapping("/at/fase6-enviar-oferta/{id}")
    public String atEnviarOferta(@PathVariable Long id) {
        Candidato c = candidatoRepo.findById(id).orElseThrow();
        c.setStatusProceso("Oferta Enviada");
        candidatoRepo.save(c);
        emailService.enviarCorreoOferta(c.getCorreo(), c.getNombre());
        return "redirect:/dashboard";
    }

    @PostMapping("/candidato/fase6-firmar/{id}")
    public String candidatoFirmarOferta(@PathVariable Long id) {
        Candidato c = candidatoRepo.findById(id).orElseThrow();
        c.setStatusProceso("Oferta Firmada");
        candidatoRepo.save(c);
        return "redirect:/dashboard";
    }

    @PostMapping("/at/fase6-confirmar-ingreso/{id}")
    public String atConfirmarIngreso(@PathVariable Long id, @RequestParam String fechaIngreso) {
        Candidato c = candidatoRepo.findById(id).orElseThrow();
        c.setFechaIngreso(fechaIngreso);
        c.setStatusProceso("Contratado");
        candidatoRepo.save(c);
        emailService.enviarCorreoBienvenida(c.getCorreo(), c.getNombre(), fechaIngreso);
        return "redirect:/dashboard";
    }

    // ==========================================
    // HERRAMIENTA DE REINICIO
    // ==========================================
    @GetMapping("/reset")
    public String reiniciarDemo() {
        List<Candidato> todos = candidatoRepo.findAll();
        for(Candidato c : todos) {
            c.setStatusProceso("Nuevo");
            c.setStatusJustificacion(null);
            c.setFechaEntrevista(null);
            c.setScorePsicometrico(null);
            c.setNotasEntrevista(null);
            c.setRecomendacionAT(null);
            c.setFechaIngreso(null);
        }
        candidatoRepo.saveAll(todos);
        return "redirect:/dashboard";
    }


}