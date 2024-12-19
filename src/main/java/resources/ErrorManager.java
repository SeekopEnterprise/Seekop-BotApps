package resources;

public class ErrorManager {

    protected String errorMessage;
    protected String debugError;

    protected void registerError(String metodo, String e) {
        registerError(metodo, e, null);
    }

    protected void registerError(String metodo, Exception e) {
        registerError(metodo, e, null);
    }

    protected void registerError(String metodo, Exception e, String observations) {
        registerError(metodo, e.toString(), null);
    }

    protected void registerError(String metodo, String error, String observations) {
        System.err.println("****************************************\n");
        System.err.println("Clase:  " + getClass().getName() + "\n");
        System.err.println("Método: " + metodo + "\n");
        System.err.println("Error:  " + error + "\n");
        if (observations != null) {
            System.err.println("Observaciones:  " + observations + "\n");
        }
        this.errorMessage = error;
    }

    protected void registerIntoLog(String title, Exception e) {
        registerIntoLog(title, e.toString());
    }

    protected void registerIntoLog(String title, String message) {
        this.debugError = (title + ":  " + message + "\n");
        System.err.println(title + ":  " + message + "\n");
    }

    public String getDebugMessage() {
        return this.debugError == null ? "" : this.debugError;
    }

    public String getErrorMessage() {
        return this.errorMessage == null ? "" : this.errorMessage;
    }
}
