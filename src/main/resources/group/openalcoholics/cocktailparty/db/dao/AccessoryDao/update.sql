UPDATE accessory
SET name = :entity.name, description = :entity.description, image_link = :entity.imageLink
WHERE id = :entity.id
