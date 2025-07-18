package com.Megatram.Megatram.repository;

import com.Megatram.Megatram.Entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
    public interface PermissionRepository extends JpaRepository<Permission, Long> {

}
