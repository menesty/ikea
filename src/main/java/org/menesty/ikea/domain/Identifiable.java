package org.menesty.ikea.domain;

import javax.persistence.*;

@MappedSuperclass
public class Identifiable {

    @Id
    @GeneratedValue(generator = "address_id", strategy = GenerationType.IDENTITY)
    private Integer id;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

}
