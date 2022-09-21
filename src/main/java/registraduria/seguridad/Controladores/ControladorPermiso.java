package registraduria.seguridad.Controladores;
import registraduria.seguridad.Modelos.Permiso;
import registraduria.seguridad.Modelos.PermisosRoles;
import registraduria.seguridad.Repositorios.RepositorioPermiso;
import registraduria.seguridad.Repositorios.RepositorioPermisosRoles;
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
    @Autowired
    private RepositorioPermisosRoles repositorioPermisosRoles;
    String metodos[] = {"POST", "GET", "PUT", "DELETE"};

    @GetMapping("")
    public List<Permiso> index(){
        return this.miRepositoriopermiso.findAll();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Permiso create(@RequestBody  Permiso infopermiso){
        if (!this.validarPermiso(infopermiso)){
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,"El método ingresado no es valido");
        }
        return this.miRepositoriopermiso.save(infopermiso);
    }

    private boolean validarPermiso(Permiso infopermiso){
        for (String metodo:this.metodos) {
            if(metodo.equals(infopermiso.getMetodo())){
                return true; //Encontró uno valido
            }
        }
        return false;
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
            if (this.validarEliminarPermiso(permisoActual)){
                throw new ResponseStatusException(HttpStatus.IM_USED, "El rol esta presente en un permiso-rol, NO SE ELIMINÓ");
            }else {
                this.miRepositoriopermiso.delete(permisoActual);
                throw new ResponseStatusException(HttpStatus.OK, "Permiso eliminado correctamente");
            }
        }
    }

    private boolean validarEliminarPermiso(Permiso infoPermiso){
        for (PermisosRoles permisorol: this.repositorioPermisosRoles.findAll()) {
            if (permisorol.getPermiso() != null)
                if (permisorol.getPermiso().get_id().equals(infoPermiso.get_id()))
                    return true;    
        }
        return false;
    }

}
