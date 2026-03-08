package com.example.final_project.features.folder;

import com.example.final_project.domain.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Integer> {
    List<Folder> findAllByWorkspaceId(Integer workspaceId);
}
