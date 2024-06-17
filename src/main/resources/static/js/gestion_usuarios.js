const fetchUsers = async (page = 0) => {
    const itemsPerPageElement = document.getElementById('itemsPerPage');
    const filterStatusElement = document.getElementById('filterStatus');
    const searchDniElement = document.getElementById('searchDni');
    const searchNameElement = document.getElementById('searchName');
    const searchEmailElement = document.getElementById('searchEmail');
    const totalUsersElement = document.getElementById('totalUsers');

    if (!itemsPerPageElement || !filterStatusElement || !searchDniElement || !searchNameElement || !searchEmailElement || !totalUsersElement) {
        console.error('One or more required elements are missing');
        return;
    }

    const itemsPerPage = itemsPerPageElement.value || 5; // Valor por defecto de 5
    const status = filterStatusElement.value || 'active'; // Aplicar filtro por defecto
    const dni = searchDniElement.value;
    const name = searchNameElement.value;
    const email = searchEmailElement.value;

    const params = new URLSearchParams({
        size: itemsPerPage,
        page: page,
        status: status !== 'all' ? status : 'active',
        dni: dni || '',
        name: name || '',
        email: email || ''
    });

    try {
        const response = await fetch(`/api/users?${params.toString()}`);
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        const data = await response.json();

        if (!data.content) {
            throw new Error('Data content is undefined');
        }

        const userTable = document.getElementById('userTable').querySelector('tbody');
        const pagination = document.getElementById('pagination');

        // Renderizar tabla de usuarios
        userTable.innerHTML = data.content.map(user => `
           <tr>
                <td>${user.activo ? 'Activo' : 'Desactivado'}</td>
                <td>${user.nombre}</td>
                <td>${user.apellido}</td>
                <td>${user.dni}</td>
                <td>${user.email}</td>
                <td>${user.fechaNacimiento}</td>
                <td>${user.rol}</td>
                <td>
                    <div class="dropdown">
                        <button class="btn btn-secondary dropdown-toggle" type="button" id="dropdownMenuButton${user.id}" data-bs-toggle="dropdown" aria-expanded="false">
                            <i class="fas fa-cog"></i>
                        </button>
                        <ul class="dropdown-menu" aria-labelledby="dropdownMenuButton${user.id}">
                            ${user.activo ? `
                                <li><a class="dropdown-item" href="#" onclick="openResetPasswordPopup(${user.id})">Restablecer Contraseña</a></li>
                                <li><a class="dropdown-item" href="#" onclick="openDeleteUserPopup(${user.id})">Eliminar</a></li>
                                <li><a class="dropdown-item" href="#" onclick="deactivateUser(${user.id})">Desactivar</a></li>
                            ` : `
                                <li><a class="dropdown-item" href="#" onclick="activateUser(${user.id})">Activar</a></li>
                            `}
                        </ul>
                    </div>
                </td>
            </tr>
        `).join('');

        // Renderizar paginación
        pagination.innerHTML = `
            <li class="page-item ${data.pageable.pageNumber === 0 ? 'disabled' : ''}">
                <a class="page-link" href="#" onclick="fetchUsers(${data.pageable.pageNumber - 1})">Previous</a>
            </li>
            ${Array.from({ length: data.totalPages }, (_, i) => `
                <li class="page-item ${i === data.pageable.pageNumber ? 'active' : ''}">
                    <a class="page-link" href="#" onclick="fetchUsers(${i})">${i + 1}</a>
                </li>
            `).join('')}
            <li class="page-item ${data.pageable.pageNumber + 1 === data.totalPages ? 'disabled' : ''}">
                <a class="page-link" href="#" onclick="fetchUsers(${data.pageable.pageNumber + 1})">Next</a>
            </li>
        `;
        // Mostrar cantidad total de usuarios
        totalUsersElement.textContent = `Total de usuarios: ${data.totalElements}`;
    } catch (error) {
        console.error('Error fetching users:', error);
    }
};

document.addEventListener('DOMContentLoaded', function() {
    // Asegurar que se seleccione "Activado" por defecto
    document.getElementById('filterStatus').value = 'active';
    document.getElementById('itemsPerPage').value = '5'; // Valor por defecto de 5

    fetchUsers();

    document.getElementById('btnSearch').addEventListener('click', function() {
        fetchUsers();
    });

    document.getElementById('btnReset').addEventListener('click', function() {
        document.getElementById('filterStatus').value = 'all';
        document.getElementById('searchDni').value = '';
        document.getElementById('searchName').value = '';
        document.getElementById('searchEmail').value = '';
        document.getElementById('itemsPerPage').value = '5'; // Restablecer a 5
        fetchUsers();
    });

    const itemsPerPageElement = document.getElementById('itemsPerPage');
    if (itemsPerPageElement) {
        itemsPerPageElement.addEventListener('change', function() {
            fetchUsers();
        });
    } else {
        console.error('itemsPerPage element is missing');
    }

    document.getElementById('btnCreateUser').addEventListener('click', function() {
        new bootstrap.Modal(document.getElementById('createUserModal')).show();
    });

    document.getElementById('btnExportCurrent').addEventListener('click', function() {
        exportUsers(false);
    });

    document.getElementById('btnExportAll').addEventListener('click', function() {
        exportUsers(true);
    });

    fetchUsers();

});

// Funciones de manejo de operaciones de usuario
function openResetPasswordPopup(userId) {
    document.getElementById('resetPasswordUserId').value = userId;
    const resetPasswordModal = new bootstrap.Modal(document.getElementById('resetPasswordModal'));
    resetPasswordModal.show();
}

async function resetPassword() {
    const userId = document.getElementById('resetPasswordUserId').value;
    const newPassword = document.getElementById('newPassword').value;
    const confirmPassword = document.getElementById('confirmPassword').value;

    if (newPassword !== confirmPassword) {
        alert('Las contraseñas no coinciden');
        return;
    }

    try {
        const response = await fetch(`/api/users/${userId}/reset-password`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ newPassword: newPassword })
        });
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        alert('Contraseña restablecida correctamente');
        const resetPasswordModal = bootstrap.Modal.getInstance(document.getElementById('resetPasswordModal'));
        resetPasswordModal.hide(); // Cerrar el modal
        fetchUsers();
    } catch (error) {
        console.error('Error resetting password:', error);
    }
}

function openDeleteUserPopup(userId) {
    document.getElementById('deleteUserId').value = userId;
    const deleteUserModal = new bootstrap.Modal(document.getElementById('deleteUserModal'));
    deleteUserModal.show();
}

async function deleteUser() {
    const userId = document.getElementById('deleteUserId').value;

    try {
        const response = await fetch(`/api/users/${userId}`, {
            method: 'DELETE'
        });
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        alert('Usuario eliminado correctamente');
        const deleteUserModal = bootstrap.Modal.getInstance(document.getElementById('deleteUserModal'));
        deleteUserModal.hide(); // Cerrar el modal
        fetchUsers();
    } catch (error) {
        console.error('Error deleting user:', error);
    }
}

async function deactivateUser(userId) {
    try {
        const response = await fetch(`/api/users/${userId}/deactivate`, { method: 'POST' });
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        fetchUsers(); // Actualizar la lista de usuarios después de la operación
    } catch (error) {
        console.error('Error deactivating user:', error);
    }
}

async function activateUser(userId) {
    try {
        const response = await fetch(`/api/users/${userId}/activate`, { method: 'POST' });
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        fetchUsers(); // Actualizar la lista de usuarios después de la operación
    } catch (error) {
        console.error('Error activating user:', error);
    }
}

async function exportUsers(all) {
    const itemsPerPageElement = document.getElementById('itemsPerPage');
    const filterStatusElement = document.getElementById('filterStatus');
    const searchDniElement = document.getElementById('searchDni');
    const searchNameElement = document.getElementById('searchName');
    const searchEmailElement = document.getElementById('searchEmail');

    if (!itemsPerPageElement || !filterStatusElement || !searchDniElement || !searchNameElement || !searchEmailElement) {
        console.error('One or more required elements are missing');
        return;
    }

    const itemsPerPage = itemsPerPageElement.value || 5; // Valor por defecto de 5
    const status = filterStatusElement.value || 'active'; // Aplicar filtro por defecto
    const dni = searchDniElement.value;
    const name = searchNameElement.value;
    const email = searchEmailElement.value;

    const params = new URLSearchParams({
        all: all,
        size: itemsPerPage,
        page: 0,
        status: status !== 'all' ? status : 'active',
        dni: dni || '',
        name: name || '',
        email: email || ''
    });

    const url = `/api/users/export?${params.toString()}`;
    window.location.href = url;
}

async function createUser() {
    const name = document.getElementById('newUserName').value;
    const lastName = document.getElementById('newUserLastName').value;
    const dni = document.getElementById('newUserDni').value;
    const birthDate = document.getElementById('newUserBirthDate').value;
    const email = document.getElementById('newUserEmail').value;
    const password = document.getElementById('newUserPassword').value;
    const role = document.getElementById('newUserRole').value;

    const user = {
        nombre: name,
        apellido: lastName,
        dni: dni,
        fechaNacimiento: birthDate,
        email: email,
        pwd: password,
        rol: role
    };

    try {
        const response = await fetch('/users/register_by_admin', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(user)
        });

        const result = await response.json();
        if (response.ok) {
            alert(result.message);
            document.getElementById('createUserModal').querySelector('.btn-close').click();
            fetchUsers();
        } else {
            // Mostrar mensajes de error específicos
            const errorFields = ['nombre', 'apellido', 'dni', 'fechaNacimiento', 'email', 'pwd'];
            errorFields.forEach(field => {
                const errorElement = document.getElementById(`${field}Error`);
                if (result[field]) {
                    errorElement.textContent = result[field];
                    errorElement.classList.remove('d-none');
                } else {
                    errorElement.textContent = '';
                    errorElement.classList.add('d-none');
                }
            });
            document.getElementById('createUserError').textContent = result.status === 'error' ? result.message : 'Error al crear el usuario.';
            document.getElementById('createUserError').classList.remove('d-none');
        }
    } catch (error) {
        console.error('Error creating user:', error);
        document.getElementById('createUserError').textContent = 'Error al crear el usuario.';
        document.getElementById('createUserError').classList.remove('d-none');
    }
}
