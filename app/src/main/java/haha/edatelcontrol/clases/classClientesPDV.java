package haha.edatelcontrol.clases;

/**
 * Created by User on 29/01/2018.
 */

public class classClientesPDV {

    private int Id;
    private String Nombre;
    private String Propietario;
    private String Numero;
    private String Otronumero;
    private String Coordenadas;
    private String Ciudad;
    private int IddDepto;

    public classClientesPDV(int id, String nombre, String numero) {
        Id = id;
        Nombre = nombre;
        Numero = numero;
    }

    public classClientesPDV(int id, String nombre, String numero, String coordenadas) {
        Id = id;
        Nombre = nombre;
        Numero = numero;
        Coordenadas = coordenadas;
    }

    public classClientesPDV(int id, String nombre, String propietario, String numero, String otronumero, String coordenadas, String ciudad, int iddDepto) {
        Id = id;
        Nombre = nombre;
        Propietario = propietario;
        Numero = numero;
        Otronumero = otronumero;
        Coordenadas = coordenadas;
        Ciudad = ciudad;
        IddDepto = iddDepto;
    }

    public String getNombre() {
        return Nombre;
    }

    public String getNumero() {
        return Numero;
    }

    public int getId() {
        return Id;
    }

    public String getCiudad() {
        return Ciudad;
    }

    public int getIddDepto() {
        return IddDepto;
    }

    public String getPropietario() {
        return Propietario;
    }

    public String getOtronumero() {
        return Otronumero;
    }

    public String getCoordenadas() {
        return Coordenadas;
    }
}
