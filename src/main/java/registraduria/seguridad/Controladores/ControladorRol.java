package registraduria.seguridad.Controladores;
import registraduria.seguridad.Modelos.Rol;
import registraduria.seguridad.Modelos.Usuario;
import registraduria.seguridad.Modelos.PermisosRoles;
import registraduria.seguridad.Repositorios.RepositorioRol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import registraduria.seguridad.Repositorios.RepositorioUsuario;
import registraduria.seguridad.Repositorios.RepositorioPermisosRoles;

import java.sql.SQLOutput;
import java.util.List;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@CrossOrigin
@RestController
@RequestMapping("/roles")

public class ControladorRol {
    @Autowired
    private RepositorioRol miRepositorioRol;
    @Autowired
    private RepositorioUsuario mirepositorioUsuario;
    @Autowired
    private RepositorioPermisosRoles mirepositorioPermisosRoles;



    @GetMapping("")
    public List<Rol> index(){
        return this.miRepositorioRol.findAll();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Rol create(@RequestBody  Rol infoRol){
        if (this.rolDistinto(infoRol)){
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,"El nombre del rol ya coincide con uno creado");
        }
        return this.miRepositorioRol.save(infoRol);
    }

    private boolean rolDistinto(Rol infoRol){
        for (Rol rol: this.miRepositorioRol.findAll())
            if (infoRol.getNombre().equals(rol.getNombre()))
                return true;
        return false;
    }

    @GetMapping("{id}")
    public Rol show(@PathVariable String id){
        Rol rolActual=this.miRepositorioRol.findById(id).orElse(null);
        if (rolActual==null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"El rol no fue encontrado");
        }
        return rolActual;
    }
    @PutMapping("{id}")
    public Rol update(@PathVariable String id,@RequestBody  Rol infoRol){
        Rol rolActual=this.miRepositorioRol.findById(id).orElse(null);
        if (rolActual!=null){
            rolActual.setNombre(infoRol.getNombre());
            rolActual.setDescripcion(infoRol.getDescripcion());
            return this.miRepositorioRol.save(rolActual);
        }else{
            return  null;
        }
    }
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("{id}")
    public void delete(@PathVariable String id){
        Rol rolActual=this.miRepositorioRol.findById(id).orElse(null);
        if (rolActual!=null){
            System.out.println("Entro al metodo y esta fue a respuesta: "+this.validarEliminarRol(rolActual));
            if (this.validarEliminarRol(rolActual)){
                throw new ResponseStatusException(HttpStatus.IM_USED, "El rol esta presente en un usuario o en un permiso-rol, NO SE ELIMINÓ");
            }else{
                this.miRepositorioRol.delete(rolActual);
                throw new ResponseStatusException(HttpStatus.OK, "Rol eliminado correctamente");
            }
        }
    }

    //Este metodo valida si un rol se ha asignado a un usuario o a un permiso rol
    private boolean validarEliminarRol(Rol infoRol){
        System.out.println("Entro al método");
        boolean rolInUsuario = false;
        boolean rolInPermisosRoles = false;
        for (Usuario usuario: this.mirepositorioUsuario.findAll()) {
            System.out.println(usuario.getRol());
            if (usuario.getRol() != null)
                if (usuario.getRol().get_id().equals(infoRol.get_id()))
                    rolInUsuario = true;
        }
        for (PermisosRoles permisoRol: this.mirepositorioPermisosRoles.findAll()) {
            if (permisoRol.getRol() != null)
                if (permisoRol.getRol().get_id().equals(infoRol.get_id()))
                    rolInPermisosRoles = true;
        }
        System.out.println("El resultado del metodo es: "+(rolInUsuario||rolInPermisosRoles));
        return rolInUsuario || rolInPermisosRoles;
    }

}
