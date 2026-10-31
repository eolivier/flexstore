// Saves a file received from the API (a PDF...) under the given name, as a download of the browser
export function saveFile(content: Blob, fileName: string) {
  const url = URL.createObjectURL(content);
  const link = document.createElement('a');
  link.href = url;
  link.download = fileName;
  link.click();
  URL.revokeObjectURL(url);
}
