package model;

import java.time.LocalDateTime;

/**
 * clasa imutabila care reprezinta o factura generata pentru fiecare comanda
 * este implementata ca record pentru a asigura imutabilitatea
 * facturile sunt stocate in tabela log si nu pot fi modificate dupa creare
 *
 * @param logId        identificatorul unic al facturii
 * @param orderId      id-ul comenzii asociate facturii
 * @param clientName   numele clientului care a plasat comanda
 * @param clientEmail  emailul clientului
 * @param orderDate    data si ora la care a fost generata factura
 * @param totalAmount  suma totala a comenzii
 */

public record Bill(
        int logId,
        int orderId,
        String clientName,
        String clientEmail,
        LocalDateTime orderDate,
        int totalAmount
) {
}