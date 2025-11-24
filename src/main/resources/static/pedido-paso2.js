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

    function luhnCheck(cardNumber) {
        // simple Luhn algorithm to validate card numbers
        const s = cardNumber.replace(/\D/g, '');
        let sum = 0;
        let shouldDouble = false;
        for (let i = s.length - 1; i >= 0; i--) {
            let digit = parseInt(s.charAt(i), 10);
            if (shouldDouble) {
                digit *= 2;
                if (digit > 9) digit -= 9;
            }
            sum += digit;
            shouldDouble = !shouldDouble;
        }
        return (sum % 10) === 0;
    }

    function validatePaso2(form) {
        let valid = true;
        const titular = getField(form, 'titular');
        const numero = getField(form, 'numero');

        if (!titular.value || titular.value.trim().length < 3) {
            showError(titular, 'Ingresa el nombre del titular (mínimo 3 caracteres).');
            valid = false;
        } else {
            clearError(titular);
        }

        const numOnly = (numero.value || '').replace(/\s+/g, '');
        if (!numOnly || numOnly.length < 12) {
            showError(numero, 'Número de tarjeta inválido.');
            valid = false;
        } else if (!luhnCheck(numOnly)) {
            showError(numero, 'Número de tarjeta no válido según Luhn.');
            valid = false;
        } else {
            clearError(numero);
        }

        return valid;
    }

    function savePaso2(form) {
        const data = {
            titular: getField(form, 'titular').value || '',
            numero: getField(form, 'numero').value || '',
            tipo: getField(form, 'tipoTarjeta') ? getField(form, 'tipoTarjeta').value : ''
        };
        try {
            localStorage.setItem('pedidoPaso2', JSON.stringify(data));
        } catch (e) {
            console.warn('No se pudo guardar en localStorage', e);
        }
    }

    function restorePaso2(form) {
        try {
            const raw = localStorage.getItem('pedidoPaso2');
            if (!raw) return;
            const data = JSON.parse(raw);
            if (data.titular) getField(form, 'titular').value = data.titular;
            if (data.numero) getField(form, 'numero').value = data.numero;
            if (data.tipo && getField(form, 'tipoTarjeta')) getField(form, 'tipoTarjeta').value = data.tipo;
        } catch (e) {
            console.warn('No se pudo leer localStorage', e);
        }
    }

    onReady(function () {
        const form = document.querySelector('form');
        if (!form) return;

        restorePaso2(form);

        form.addEventListener('input', function (e) {
            validatePaso2(form);
        });

        form.addEventListener('blur', function (e) {
            savePaso2(form);
        }, true);

        form.addEventListener('submit', function (e) {
            const ok = validatePaso2(form);
            if (!ok) {
                e.preventDefault();
                e.stopPropagation();
                return false;
            }

            savePaso2(form);
            return true;
        });
    });
})();

