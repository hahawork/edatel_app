package haha.edatelcontrol.clases;

/**
 * Created by User on 05/02/2018.
 */

public class classVenta {


    private int idVenta, codUsuario, Pdv;
    private String numeroPOS, Cantidad, TotalCobrarse, Fecha, tipoVentaCntdCrdt;

    /**
     * instancia de la clase para nuevo objeto
     * @param idVenta
     * @param codUsuario
     * @param pdv
     * @param numeroPOS
     * @param cantidad
     * @param fecha
     * @param tipoVentaCntdCrdt
     */
    public classVenta(int idVenta, int codUsuario, int pdv, String numeroPOS, String cantidad, String totalCobrarse, String fecha, String tipoVentaCntdCrdt) {
        this.idVenta = idVenta;
        this.codUsuario = codUsuario;
        Pdv = pdv;
        this.numeroPOS = numeroPOS;
        Cantidad = cantidad;
        TotalCobrarse=totalCobrarse;
        Fecha = fecha;
        this.tipoVentaCntdCrdt = tipoVentaCntdCrdt;
    }

    public int getIdVenta() {
        return idVenta;
    }

    public int getCodUsuario() {
        return codUsuario;
    }

    public int getPdv() {
        return Pdv;
    }

    public String getNumeroPOS() {
        return numeroPOS;
    }

    public String getCantidad() {
        return Cantidad;
    }

    public String getTotalCobrarse() {
        return TotalCobrarse;
    }

    public String getFecha() {
        return Fecha;
    }

    public String getTipoVentaCntdCrdt() {
        return tipoVentaCntdCrdt;
    }
}
