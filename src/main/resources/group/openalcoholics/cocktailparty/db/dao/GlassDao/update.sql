UPDATE glass
SET name = :entity.name,
    estimated_size = :entity.estimatedSize,
    image_link = :entity.imageLink
WHERE id = :entity.id
