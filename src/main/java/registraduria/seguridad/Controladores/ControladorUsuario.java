package registraduria.seguridad.Controladores;
import registraduria.seguridad.Modelos.Usuario;
import registraduria.seguridad.Modelos.Rol;
import registraduria.seguridad.Repositorios.RepositorioUsuario;
import registraduria.seguridad.Repositorios.RepositorioRol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@CrossOrigin
@RestController
@RequestMapping("/usuarios")

public class ControladorUsuario {
    @Autowired
    private RepositorioUsuario miRepositorioUsuario;
    @Autowired
    private RepositorioRol miRepositorioRol;
    @GetMapping("")
    public List<Usuario> index(){
        return this.miRepositorioUsuario.findAll();
    }
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Usuario create(@RequestBody Usuario infoUsuario){
        infoUsuario.setContrasena(convertirSHA256(infoUsuario.getContrasena()));
        if(this.nombreRepetido(infoUsuario)==true){
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,"El nombre de usuario ya existe");
        }else if(!this.cedulaValida(infoUsuario)){
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,"El correo no es correcto");
        }
        return this.miRepositorioUsuario.save(infoUsuario);
    }

    //Método para detectar un nombre repetido
    private boolean nombreRepetido(Usuario infousuario){
        System.out.println("Entro al método");
        for(Usuario user : this.miRepositorioUsuario.findAll()){
            System.out.println(user.getNombre()+" "+infousuario.getNombre());
            if(user.getNombre().equals(infousuario.getNombre())){
                return true;
            }
        }
        return false;
    }
    //Método para detectar si el correo es valido usando una expresión regular
    private boolean cedulaValida(Usuario infousuario){
        Pattern pattern = Pattern.compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                        + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
        String email = infousuario.getCorreo();
        Matcher mather = pattern.matcher(email);
        if(mather.find() == true){
            return true;  //es valido
        }else{
            return false;
        }
    }


    @GetMapping("{id}")
    public Usuario show(@PathVariable String id){
        Usuario usuarioActual=this.miRepositorioUsuario.findById(id).orElse(null);
        if (usuarioActual==null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"El usuario no fue encontrado");
        }
        return usuarioActual;
    }
    @PutMapping("{id}")
    public Usuario update(@PathVariable String id,@RequestBody Usuario infoUsuario){
        Usuario usuarioActual=this.miRepositorioUsuario
                .findById(id)
                .orElse(null);
        if (usuarioActual!=null){
            usuarioActual.setCedula(infoUsuario.getCedula());
            usuarioActual.setNombre(infoUsuario.getNombre());
            usuarioActual.setApellido(infoUsuario.getApellido());
            usuarioActual.setCorreo(infoUsuario.getCorreo());
            usuarioActual.setContrasena(convertirSHA256(infoUsuario.getContrasena()));
            return this.miRepositorioUsuario.save(usuarioActual);
        }else{
        return null;
    }
}
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("{id}")
    public void delete(@PathVariable String id){
        Usuario usuarioActual=this.miRepositorioUsuario
                .findById(id)
                .orElse(null);
        if (usuarioActual!=null){
            this.miRepositorioUsuario.delete(usuarioActual);
            throw new ResponseStatusException(HttpStatus.OK,"Usuario eliminado correctamente");
        }else{
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"El usuario no fue encontrado");
        }
    }

    /**
     * Relación (1 a n) entre rol y usuario
     * @param id
     * @param id_rol
     * @return
     */
    @PutMapping("{id}/rol/{id_rol}")
    public Usuario asignarRolAUsuario(@PathVariable String id,@PathVariable String id_rol){
        Usuario usuarioActual=this.miRepositorioUsuario.findById(id).orElse(null);
        if(usuarioActual==null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"El usuario solicitado no existe");
        }
        Rol rolActual=this.miRepositorioRol.findById(id_rol).orElse(null);
        if(rolActual==null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"El rol solicitado no existe");
        }
        usuarioActual.setRol(rolActual);
        return this.miRepositorioUsuario.save(usuarioActual);
    }
    public String convertirSHA256(String password) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
        byte[] hash = md.digest(password.getBytes());
        StringBuffer sb = new StringBuffer();
        for(byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    @PostMapping("/validar")
    public Usuario validate(@RequestBody  Usuario infoUsuario,
                            final HttpServletResponse response) throws IOException {
        Usuario usuarioActual=this.miRepositorioUsuario
                .getUserByEmail(infoUsuario.getCorreo());
        if (usuarioActual!=null &&
                usuarioActual.getContrasena().equals(convertirSHA256(infoUsuario.getContrasena()))) {
            usuarioActual.setContrasena("");
            return usuarioActual;
        }else{
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }
    }
}
