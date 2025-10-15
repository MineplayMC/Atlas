package be.esmay.atlas.base.scaler;

public final class ServerNaming {

    private final String name;
    private final String identifier;

    public ServerNaming(String name, String identifier) {
        this.name = name;
        this.identifier = identifier;
    }

    public String getName() {
        return this.name;
    }

    public String getIdentifier() {
        return this.identifier;
    }
}
