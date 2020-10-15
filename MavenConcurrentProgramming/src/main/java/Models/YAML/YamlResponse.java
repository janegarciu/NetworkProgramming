package Models.YAML;

import Models.Person;

public class YamlResponse extends Person {
    private int id;
    private String first_name;
    private String last_name;
    public String card_number;
    public String card_balance;
    public String card_currency;

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
    public String getCard_number() {
        return card_number;
    }

    @Override
    public void setCard_number(String card_number) {
        this.card_number = card_number;
    }

    @Override
    public String getCard_balance() {
        return card_balance;
    }

    @Override
    public void setCard_balance(String card_balance) {
        this.card_balance = card_balance;
    }

    @Override
    public String getCard_currency() {
        return card_currency;
    }

    @Override
    public void setCard_currency(String card_currency) {
        this.card_currency = card_currency;
    }
}