import type { RequestEvent } from "@sveltejs/kit";
import { jsPDF } from "jspdf";
import qrcode from "qrcode-generator";
import fs from "fs";

/**
 * Data required to generate a QR code PDF document.
 * @property {string} roomName - The name of the room where the plant is located.
 * @property {string} plantName - The name of the plant being monitored.
 * @property {string} logoBase64 - A base64 encoded image of the logo to be displayed on the PDF document.
 * @property {string} qrcodeBase64 - A base64 encoded image of the QR code to be displayed on the PDF document.
 * @property {string} url - The URL that was encoded in the QR code.
 */
type QrCodePdfData = {
  roomName: string;
  plantName: string;
  logoBase64: string;
  qrcodeBase64: string;
  sensorStationId: string;
  url: string;
};

/**
 * Generates a QR code PDF for the given request event.
 *
 * @param {RequestEvent} event The request event containing query parameters.
 * @returns {Promise<Response>} A response with the generated PDF buffer as content.
 */
export async function GET({ url }: RequestEvent) {
  const sensorStationId = url.searchParams.get("sensorStationId") ?? "";
  const roomName = url.searchParams.get("roomName") ?? "";
  const plantName = url.searchParams.get("plantName") ?? "";
  let logoBase64 = await fs.promises.readFile(
    "src/lib/assets/logoBase64.txt", // NOTE: importing it via $assets/logoBase64.txt adds a '/' infront and wont allow for the file to open
    "utf8"
  );
  const baseURL = `${url.origin}/guest/plant`;
  let URL = `${baseURL}?sensorStationId=${sensorStationId}`;

  let qrcodeBase64: string = createQrCode(URL);
  const data: QrCodePdfData = {
    roomName,
    plantName,
    logoBase64,
    qrcodeBase64,
    sensorStationId,
    url: URL,
  };

  let pdfBuffer = await generateQrCodePdfBuffer(data);

  let responseHeaders = new Headers();
  responseHeaders.append("Content-Type", "application/pdf");
  responseHeaders.append("Content-Length", pdfBuffer.length.toString());
  responseHeaders.append(
    "Content-Disposition",
    `attachment; filename="qr_code_${sensorStationId}.pdf"`
  );

  let responseOptions = {
    status: 200,
    statusText: "OK",
    headers: responseHeaders,
  };

  return new Response(pdfBuffer, responseOptions);
}

/**
 * Generates a QR code PDF blob for the given QR code PDF data.
 *
 * @param {QrCodePdfData} data The QR code PDF data to generate a blob for.
 * @returns {Buffer} The generated PDF buffer.
 */
async function generateQrCodePdfBuffer(data: QrCodePdfData): Promise<Buffer> {
  let pdf = new jsPDF("portrait", "mm", "A6");
  pdf.addMetadata("Page Size", "A6");

  let heading = "Plant Health";
  pdf.setFontSize(20);
  pdf.text(heading, center(pdf, pdf.getTextWidth(heading)), 10);

  pdf.addImage(data.logoBase64, "PNG", center(pdf, 30), 15, 30, 30);

  pdf.setFontSize(10);
  pdf.text(`Room: ${data.roomName}`, 20, 55);

  pdf.setFontSize(10);
  pdf.text(`Plant: ${data.plantName}`, 20, 60);

  pdf.line(15, 65, pdf.internal.pageSize.width - 15, 65);

  pdf.addImage(data.qrcodeBase64, "PNG", center(pdf, 50), 75, 50, 50);

  let idString = `ID = ${data.sensorStationId}`;
  pdf.setFontSize(6);
  pdf.text(idString, center(pdf, pdf.getTextWidth(idString)), 135);

  return Buffer.from(pdf.output("arraybuffer"));
}

/**
 * Creates a base64 encoded image of a QR code for a given URL.
 * @param {string} url - The URL to be encoded in the QR code.
 * @returns {string} The base64 encoded image of the QR code.
 */
function createQrCode(url: string): string {
  let typeNumber: TypeNumber = 0;
  let errorCorrectionLevel: ErrorCorrectionLevel = "H";
  let qr = qrcode(typeNumber, errorCorrectionLevel);
  qr.addData(url);
  qr.make();
  let base64Image = qr.createDataURL();

  // NOTE: createDataURL writes data:image/gif which we don't want
  return "data:image/;" + base64Image.split(";")[1];
}

function center(pdf: jsPDF, itemWidth: number) {
  return pdf.internal.pageSize.width / 2 - itemWidth / 2;
}
