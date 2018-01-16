package com.budi.go_learn.Models;

import android.graphics.Bitmap;

/**
 * Created by root on 12/28/17.
 */

public class mPengajar {
    private String id, name, email, phone, gender, address, pelajaran, keterangan, active, work;
    private Bitmap foto;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getGender() {
        return gender;
    }

    public String getAddress() {
        return address;
    }

    public String getPelajaran() {
        return pelajaran;
    }

    public String getKeterangan() {
        return keterangan;
    }

    public Bitmap getFoto() {
        return foto;
    }

    public String getActive() {
        return active;
    }

    public String getWork() {
        return work;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPelajaran(String pelajaran) {
        this.pelajaran = pelajaran;
    }

    public void setKeterangan(String keterangan) {
        this.keterangan = keterangan;
    }

    public void setFoto(Bitmap foto) {
        this.foto = foto;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public void setWork(String work) {
        this.work = work;
    }
}