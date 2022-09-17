package registraduria.seguridad.Controladores;
import registraduria.seguridad.Modelos.Permiso;
import registraduria.seguridad.Repositorios.RepositorioPermiso;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


import java.util.List;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@CrossOrigin
@RestController
@RequestMapping("/permisos")

public class ControladorPermiso {
    @Autowired
    private RepositorioPermiso miRepositoriopermiso;


    @GetMapping("")
    public List<Permiso> index(){
        return this.miRepositoriopermiso.findAll();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Permiso create(@RequestBody  Permiso infopermiso){
        return this.miRepositoriopermiso.save(infopermiso);
    }
    @GetMapping("{id}")
    public Permiso show(@PathVariable String id){
        Permiso permisoActual=this.miRepositoriopermiso.findById(id).orElse(null);
        if (permisoActual==null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"El permiso no fue encontrado");
        }
        return permisoActual;
    }
    @PutMapping("{id}")
    public Permiso update(@PathVariable String id,@RequestBody  Permiso infopermiso){
        Permiso permisoActual=this.miRepositoriopermiso.findById(id).orElse(null);
        if (permisoActual!=null){
            permisoActual.setMetodo(infopermiso.getMetodo());
            permisoActual.setUrl(infopermiso.getUrl());
            return this.miRepositoriopermiso.save(permisoActual);
        }else{
            return  null;
        }
    }
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("{id}")
    public void delete(@PathVariable String id){
        Permiso permisoActual=this.miRepositoriopermiso.findById(id).orElse(null);
        if (permisoActual!=null){
            this.miRepositoriopermiso.delete(permisoActual);
        }
    }
}
