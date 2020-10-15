package Models.XML;

import Models.Person;

public class Record extends Person {
    private int id;
    private String first_name;
    private String last_name;
    private String full_name;
    private String bitcoin_address;

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getFirst_name() {
        return first_name;
    }

    @Override
    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    @Override
    public String getLast_name() {
        return last_name;
    }

    @Override
    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    @Override
    public String getFull_name() {
        return full_name;
    }

    @Override
    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    @Override
    public String getBitcoin_address() {
        return bitcoin_address;
    }

    @Override
    public void setBitcoin_address(String bitcoin_address) {
        this.bitcoin_address = bitcoin_address;
    }
}
