(function () {
    'use strict';

    // Helper: wait for DOM ready
    function onReady(fn) {
        if (document.readyState !== 'loading') fn();
        else document.addEventListener('DOMContentLoaded', fn);
    }

    // Convert pixels to mm for jsPDF when using 'pt' base. We'll use a simple ratio.
    function pxToMm(px) {
        // 1px = 0.264583 mm (approx)
        return px * 0.264583;
    }

    async function generatePdfFromElement(element) {
        // ensure element exists
        if (!element) throw new Error('Elemento para generar PDF no encontrado');

        // Use html2canvas to render the element to canvas
        const scale = 2; // increase for better quality
        const canvas = await html2canvas(element, { scale: scale, useCORS: true });

        const imgData = canvas.toDataURL('image/jpeg', 0.95);

        // Create jsPDF instance (robust detection)
        let jsPDFConstructor = null;
        if (window.jspdf) {
            jsPDFConstructor = window.jspdf.jsPDF || window.jspdf.default || window.jspdf;
        } else if (window.jsPDF) {
            jsPDFConstructor = window.jsPDF;
        }
        if (!jsPDFConstructor) throw new Error('jsPDF no está disponible en la página');

        const pdf = new jsPDFConstructor('p', 'mm', 'a4');

        const pageWidth = pdf.internal.pageSize.getWidth();
        const pageHeight = pdf.internal.pageSize.getHeight();

        // Canvas size in px
        const canvasWidth = canvas.width;
        const canvasHeight = canvas.height;

        // Convert canvas px to mm
        const imgWidthMm = pxToMm(canvasWidth / scale);
        const imgHeightMm = pxToMm(canvasHeight / scale);

        const ratio = Math.min(pageWidth / imgWidthMm, pageHeight / imgHeightMm);
        const renderWidth = imgWidthMm * ratio;
        const renderHeight = imgHeightMm * ratio;

        // If renderHeight fits in one page, just add image
        if (renderHeight <= pageHeight) {
            pdf.addImage(imgData, 'JPEG', (pageWidth - renderWidth) / 2, 10, renderWidth, renderHeight);
        } else {
            // Paginate: slice canvas into multiple pages
            const canvasPageHeightPx = Math.floor((pageHeight / ratio) * scale);
            let remainingHeightPx = canvasHeight;
            let positionY = 0;
            while (remainingHeightPx > 0) {
                const sliceHeightPx = Math.min(canvasPageHeightPx, remainingHeightPx);

                // create temporary canvas to hold the slice
                const tmpCanvas = document.createElement('canvas');
                tmpCanvas.width = canvas.width;
                tmpCanvas.height = sliceHeightPx;
                const ctx = tmpCanvas.getContext('2d');

                ctx.drawImage(canvas, 0, positionY, canvas.width, sliceHeightPx, 0, 0, canvas.width, sliceHeightPx);

                const sliceData = tmpCanvas.toDataURL('image/jpeg', 0.95);

                const sliceHeightMm = pxToMm(sliceHeightPx / scale);
                const sliceRenderHeight = sliceHeightMm * ratio;

                if (positionY > 0) pdf.addPage();
                pdf.addImage(sliceData, 'JPEG', (pageWidth - renderWidth) / 2, 10, renderWidth, sliceRenderHeight);

                remainingHeightPx -= sliceHeightPx;
                positionY += sliceHeightPx;
            }
        }

        return pdf;
    }

    function disableButton(btn) {
        if (!btn) return;
        btn.disabled = true;
        btn.classList.add('disabled');
    }

    function enableButton(btn) {
        if (!btn) return;
        btn.disabled = false;
        btn.classList.remove('disabled');
    }

    async function onPdfButtonClick(e) {
        e.preventDefault();
        const btn = e.currentTarget;
        disableButton(btn);
        try {
            if (!window.html2canvas) {
                alert('html2canvas no está cargado. Comprueba la inclusión de la librería.');
                return;
            }
            const resumen = document.getElementById('resumen-pedido');
            if (!resumen) {
                alert('No se encontró el resumen del pedido en la página.');
                return;
            }

            const pdf = await generatePdfFromElement(resumen);

            const now = new Date();
            const timestamp = now.toISOString().replace(/[:.]/g, '-');
            const filename = `resumen-pedido-${timestamp}.pdf`;

            pdf.save(filename);
        } catch (err) {
            console.error('Error generando PDF:', err);
            alert('Ocurrió un error al generar el PDF. Revisa la consola para más detalles.');
        } finally {
            enableButton(btn);
        }
    }

    onReady(function () {
        const btn = document.getElementById('btnDescargarPdfCliente');
        if (!btn) {
            console.warn('Botón de descarga de PDF (cliente) no encontrado en la página');
        } else {
            btn.addEventListener('click', onPdfButtonClick);
        }

        // Small diagnostic: expose a global for dev console
        window.__pedido_pdf_ready = true;
    });

})();
