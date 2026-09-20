INSERT INTO bookshelf.m_user (
    id,
    name,
    mail_address,
    role_name,
    password
) VALUES (
    'satouxr',
    '佐藤　良平',
    'ryouhei.satou0@gmail.com',
    'ADMIN',
    '4ab790eeb6531a0101bee8b33159fcae0b08fd7ba7ae18f3e30db132648c5b65958fcc355aaca26b68f097a48c75679f53fb863b035d737a0b68eeb79e806591'
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO bookshelf.m_genre (
    id,
    name
) VALUES
(1, '小説'),
(2, '参考書'),
(3, '教養'),
(4, 'マンガ'),
(5, 'その他')
ON CONFLICT (id) DO NOTHING;
