package com.cadit.data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "CacheTable")
public class CacheEntity implements Serializable {


    private static final long serialVersionUID = -6244772634148072495L;
    private Long id;
    private String key;
    private String value;

    public CacheEntity() {
    }

    public CacheEntity(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Id
    @GeneratedValue(generator = "cache_id", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "cache_id", sequenceName = "cache_id", allocationSize = 1)
    @Column(name = "CACHE_ID")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "CACHE_KEY")
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Column(name = "CACHE_VALUE")
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CacheEntity that = (CacheEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(key, that.key) &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, key, value);
    }

    @Override
    public String toString() {
        return "CacheEntity{" +
                "id=" + id +
                ", key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
