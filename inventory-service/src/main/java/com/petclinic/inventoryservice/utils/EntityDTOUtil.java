package com.petclinic.inventoryservice.utils;

import com.petclinic.inventoryservice.datalayer.Bundle;
import com.petclinic.inventoryservice.datalayer.BundleDTO;
import org.springframework.beans.BeanUtils;
import java.util.UUID;
public class EntityDTOUtil {
    public static BundleDTO toDto (Bundle bundle){
        BundleDTO dto = new BundleDTO();
        BeanUtils.copyProperties(bundle, dto);
        return dto;
    }

    public static Bundle toEntity (BundleDTO dto){
        Bundle bundle = new Bundle();
        BeanUtils.copyProperties(dto, bundle);
        return bundle;
    }
    public static String generateUUID(){
        return UUID.randomUUID().toString();
    }
}

