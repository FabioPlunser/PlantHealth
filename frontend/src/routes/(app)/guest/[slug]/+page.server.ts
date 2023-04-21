export async function load({ params, url, slug }) {
  type Picture = {
    imageRef: string;
    creationDate: Date;
  };

  let plantId = url.searchParams.get("plantId");
  let plantName: string = "Sakura";
  let roomName: string = "Room 1";
  // TODO: fetch proper backend endpoint
  let imageRef = "https://picsum.photos/200/300";

  let pictures: Picture[] = [
    { imageRef, creationDate: new Date("04/03/2023") },
    { imageRef, creationDate: new Date("03/20/2023") },
    { imageRef, creationDate: new Date("03/18/2023") },
    { imageRef, creationDate: new Date("03/02/2023") },
    { imageRef, creationDate: new Date("01/04/2023") },
  ];
  return {
    plantName,
    roomName,
    pictures,
  };
}
