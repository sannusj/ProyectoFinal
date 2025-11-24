(function () {
    'use strict';

    function onReady(fn) {
        if (document.readyState !== 'loading') fn();
        else document.addEventListener('DOMContentLoaded', fn);
    }

    function getField(form, name) {
        return form.elements[name];
    }

    function showError(input, message) {
        if (!input) return;
        input.classList.add('is-invalid');
        let feedback = input.parentNode.querySelector('.invalid-feedback');
        if (!feedback) {
            feedback = document.createElement('div');
            feedback.className = 'invalid-feedback';
            input.parentNode.appendChild(feedback);
        }
        feedback.textContent = message;
    }

    function clearError(input) {
        if (!input) return;
        input.classList.remove('is-invalid');
        const feedback = input.parentNode.querySelector('.invalid-feedback');
        if (feedback) feedback.textContent = '';
    }

    function validateEmail(email) {
        return /\S+@\S+\.\S+/.test(email);
    }

    function validatePaso1(form) {
        let valid = true;
        const nombre = getField(form, 'nombre');
        const direccion = getField(form, 'direccion');
        const provincia = getField(form, 'provincia');

        // Nombre
        if (!nombre.value || nombre.value.trim().length < 3) {
            showError(nombre, 'Ingresa tu nombre completo (mínimo 3 caracteres).');
            valid = false;
        } else {
            clearError(nombre);
        }

        // Dirección
        if (!direccion.value || direccion.value.trim().length < 3) {
            showError(direccion, 'Ingresa una dirección válida.');
            valid = false;
        } else {
            clearError(direccion);
        }

        // Provincia
        if (!provincia.value || provincia.value.trim().length < 2) {
            showError(provincia, 'Selecciona o ingresa la provincia.');
            valid = false;
        } else {
            clearError(provincia);
        }

        return valid;
    }

    function savePaso1(form) {
        const data = {
            nombre: getField(form, 'nombre').value || '',
            direccion: getField(form, 'direccion').value || '',
            provincia: getField(form, 'provincia').value || ''
        };
        try {
            localStorage.setItem('pedidoPaso1', JSON.stringify(data));
        } catch (e) {
            console.warn('No se pudo guardar en localStorage', e);
        }
    }

    function restorePaso1(form) {
        try {
            const raw = localStorage.getItem('pedidoPaso1');
            if (!raw) return;
            const data = JSON.parse(raw);
            if (data.nombre) getField(form, 'nombre').value = data.nombre;
            if (data.direccion) getField(form, 'direccion').value = data.direccion;
            if (data.provincia) getField(form, 'provincia').value = data.provincia;
        } catch (e) {
            console.warn('No se pudo leer localStorage', e);
        }
    }

    onReady(function () {
        const form = document.querySelector('form');
        if (!form) return;

        // Prefill if possible
        restorePaso1(form);

        // Validate on input
        form.addEventListener('input', function (e) {
            validatePaso1(form);
        });

        // Save partial on blur
        form.addEventListener('blur', function (e) {
            savePaso1(form);
        }, true);

        form.addEventListener('submit', function (e) {
            const ok = validatePaso1(form);
            if (!ok) {
                e.preventDefault();
                e.stopPropagation();
                return false;
            }

            // Save and allow submission
            savePaso1(form);
            return true;
        });
    });
})();

