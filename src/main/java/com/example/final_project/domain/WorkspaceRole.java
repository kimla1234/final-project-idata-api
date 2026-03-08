package com.example.final_project.domain;

public enum WorkspaceRole {
    OWNER,   // មានសិទ្ធិគ្រប់យ៉ាង រួមទាំងលុប Workspace
    ADMIN,   // មានសិទ្ធិ Invite មនុស្ស និងចាត់ចែងសារ
    EDITOR,  // មានសិទ្ធិត្រឹមតែសរសេរសារ និងបូកសរុប AI
    VIEWER   // អាចមើលបានតែរបាយការណ៍ (Analytics)
}