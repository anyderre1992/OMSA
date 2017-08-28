package com.proyectoFinal.OMSA;

import com.proyectoFinal.OMSA.Entities.Autobus;
import com.proyectoFinal.OMSA.Entities.Coordenada;
import com.proyectoFinal.OMSA.Entities.Parada;
import com.proyectoFinal.OMSA.Entities.Ruta;
import com.proyectoFinal.OMSA.Services.AutobusServices;
import com.proyectoFinal.OMSA.Services.CoordenadaServices;
import com.proyectoFinal.OMSA.Services.ParadaServices;
import com.proyectoFinal.OMSA.Services.RutaServices;
import com.sun.org.apache.bcel.internal.generic.L2I;
import com.sun.org.apache.xpath.internal.operations.Mod;
import org.hibernate.internal.IteratorImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by anyderre on 31/07/17.
 */
@Controller
//@EnableWebMvc
@RequestMapping("/ruta")
public class RutaController {
    @Autowired
    private RutaServices rutaServices;

    @Autowired
    private AutobusServices autobusServices;

    @Autowired
    private ParadaServices paradaServices;

    @Autowired
    CoordenadaServices coordenadaServices;
    @RequestMapping("/")
    public String index(Model model){
        model.addAttribute("size", rutaServices.buscarTodasLasRutas().size());
        return "ver_ruta";
    }

    @RequestMapping("/listar/paradas/{id}")
    public String mostrarParadas(Model model, @PathVariable("id") Long id_ruta){
        List<Parada>paradas = paradaServices.buscarParadaPorRutaId(id_ruta);
        model.addAttribute("paradas",paradas);
        model.addAttribute("size", paradas.size());
        model.addAttribute("id_ruta", id_ruta);
        model.addAttribute("nombreCorredor", rutaServices.buscarRutaPorId(id_ruta).getNombreCorredor());
        return "ver_parada_pintada";
    }

    @RequestMapping("/listar/autobus")
    public String mostrarAutobus(Model model, @RequestParam("id")Long id_ruta){
        List<Autobus> autobuses = autobusServices.buscarTodosLosAutobusporRuta(id_ruta);
        model.addAttribute("autobuses", autobuses);
        return "ver_autobus";
    }
    @RequestMapping("/listar/coordenadas/{id}")
    public String mostrarCoordenadas(Model model, @PathVariable("id") Long id_ruta){
        List<Coordenada> coordenadas = rutaServices.buscarRutaPorId(id_ruta).getCoordenadas();
        model.addAttribute("coordenadas",coordenadas);
        model.addAttribute("size", coordenadas.size());
        model.addAttribute("id_ruta", id_ruta);
        model.addAttribute("nombreCorredor", rutaServices.buscarRutaPorId(id_ruta).getNombreCorredor());
        return "ver_coordenada_pintada";
    }

    @RequestMapping(value = "/buscar", method = RequestMethod.GET, produces = "application/json")
    public List<Ruta> buscarRuta(Model model, @RequestParam("nombre_corredor")String nombre_Corredor){
        //model.addAttribute("ruta", rutaServices.buscarRutaPorNombreCorredor(nombre_Corredor));
        return rutaServices.buscarRutaPorNombreCorredor(nombre_Corredor);
    }

    @RequestMapping("/editar/{ruta}")
    public String editarRuta(Model model , @PathVariable("ruta")Long id){
        Ruta ruta = rutaServices.buscarRutaPorId(id);
        model.addAttribute("ruta", ruta);
        return "editar_ruta";
    }

    @PostMapping("/editar")
    public String guardarRutaEditada(@ModelAttribute Ruta ruta, @RequestParam("corredor")String nombreCorredor){

        Ruta ruta1 = rutaServices.buscarRutaPorNombreCorredor(nombreCorredor).get(1);

        if(ruta1!=null){
            ruta1.setCiudad(ruta.getCiudad());
            ruta1.setNombreCorredor(ruta.getNombreCorredor());
            ruta1.setDistanciaTotal(ruta.getDistanciaTotal());
            ruta1.setFechaUltimaModificacion(getLongFromDate(new Date()));
            rutaServices.guardarRuta(ruta1);
        }
        ruta.setFechaUltimaModificacion(getLongFromDate(new Date()));
        rutaServices.guardarRuta(ruta);
       return "redirect:/ruta/";
    }

    @RequestMapping("/crear")
    public String crearRuta(Model model){
        model.addAttribute("ruta", new Ruta());
        return "crear_ruta";
    }
    @PostMapping("/crear")
    public String guardarRutaCreada(@ModelAttribute Ruta ruta){
        ruta.setFechaUltimaModificacion(getLongFromDate(new Date()));
        ruta.setFechaCreada(getLongFromDate(new Date()));
        ruta.setEsDireccionSubida(true);
        Ruta currentRuta =rutaServices.guardarRuta(ruta);

        Ruta nuevaRuta = new Ruta();
        nuevaRuta.setNombreCorredor(ruta.getNombreCorredor());
        nuevaRuta.setDistanciaTotal(ruta.getDistanciaTotal());
        nuevaRuta.setFechaUltimaModificacion(ruta.getFechaUltimaModificacion());
        nuevaRuta.setFechaCreada(ruta.getFechaCreada());
        nuevaRuta.setCiudad(ruta.getCiudad());
        nuevaRuta.setEsDireccionSubida(false);
        rutaServices.guardarRuta(nuevaRuta);
        return "redirect:/ruta/";
    }
    private Long getLongFromDate(Date date){
        return date.getTime()/1000;
    }

    @Transactional
    @RequestMapping("/eliminar/{id}")
    public String eliminarRuta(@PathVariable("id")Long id){
        paradaServices.eliminarParadaPorRutaId(id);
        List<Autobus> autobuses= autobusServices.buscarTodosLosAutobusporRuta(id);
        for(Autobus autobus: autobuses){
            autobus.setRuta(null);
            autobusServices.guardarAutobus(autobus);
        }
        rutaServices.eliminarRutaPorId(id);
        return "redirect:/ruta/";
    }

}