/*-- 1. ЮЗЕРЫ ---*/
-- переименование
--ALTER TABLE public.user RENAME TO "user";
-- удаление таблицы
--DROP TABLE public.user CASCADE;
-- создание таблицы
CREATE TABLE public.user (
	id SERIAL PRIMARY KEY,
	login varchar(50) NOT null,
	password varchar(50) NOT null,
	first_name varchar(50) null,
	last_name varchar(50) null,
	mail varchar(50) null,
	phone varchar(50) null,
	comment varchar(500) null
);

-- очистка содержимого таблицы
--delete from public.user;
-- начальное заполнение
INSERT INTO public.user
(login, password, first_name, last_name, mail, phone, comment)
values
('Vlad','1111', null,null,null,null,null),
('Anna','1111', null,null,null,null,null),
('Dima','1111', null,null,null,null,null),
('Miha','1111', null,null,null,null,null),
('Sergey','1111', null,null,null,null,null),
('Martin','1111', null,null,null,null,null),
('Luter','1111', null,null,null,null,null),
('King','1111', null,null,null,null,null),
('Vika','1111', null,null,null,null,null);

-- показать id по логину
select id
from public.user
where login = 'Sergey';
-- показать loginЫ по id
select login
from public.user
where id = 1 or id = 4 or id = 7;
-- показать id и password по логину
select id,login,password 
from public.user
where login = 'Sergey';

-- показать всё содержимое
select * from public.user;


/*-- 2. Список диалогов ---*/
-- удаление таблицы
--DROP TABLE public.chat_list CASCADE;
-- создание таблицы
CREATE TABLE public.chat_list (
	id SERIAL PRIMARY KEY,
	userid_min integer NOT null,
	userid_max integer NOT null,
	table_name varchar(70) null,
	comment varchar(500) null
);

-- удаление ключей
--ALTER TABLE public.chat_list DROP CONSTRAINT fk_userid_max;
--ALTER TABLE public.chat_list DROP CONSTRAINT fk_userid_min;
-- установка внешних ключей
ALTER TABLE  public.chat_list
ADD CONSTRAINT fk_userid_min FOREIGN KEY (userid_min)
REFERENCES public.user (id);
ALTER TABLE  public.chat_list
ADD CONSTRAINT fk_userid_max FOREIGN KEY (userid_max)
REFERENCES public.user (id);

-- очистка содержимого таблицы
--delete from public.chat_list;
-- начальное заполнение
INSERT INTO public.chat_list
(userid_min, userid_max, table_name, comment)
values
(1,5,'zz1yy5',null),
(4,7,'zz4yy7',null),
(3,6,'zz3yy6',null),
(5,7,'zz5yy7',null),
(4,5,'zz4yy5',null);

-- показать все чаты, где Sergey (id=5)
select *
from public.chat_list
where userid_min = 5 or userid_max = 5;

-- проверить наличие чата
SELECT id FROM public.chat_list WHERE table_name = 'zz5yy7';

-- получить userid_min, userid_max по id для chat_list
select userid_min, userid_max from public.chat_list where id = 1;


-- показать всё содержимое
select * from public.chat_list;


/*--- 3. ИСТОРИЯ СООБЩЕНИЙ - ДИАЛОГ ---*/
-- удаление таблицы
--DROP TABLE public.zz1yy5 CASCADE;
-- создание таблицы
CREATE TABLE IF NOT EXISTS public.zz1yy5 (
	id SERIAL PRIMARY KEY,
	mes_author_id integer NOT null,
	mes_content varchar(1000) null,
	mes_datetime timestamp NOT null,
	mes_comment varchar(256) null
);

-- 3.1. установка внешних ключей
ALTER TABLE  public.zz1yy5									--в таблице zz1yy5
ADD CONSTRAINT fk_mes_author_id FOREIGN KEY (mes_author_id)	--колонка mes_author_id
REFERENCES public.user (id);							--это тоже самое, что и колонка id из таблицы user
--todo ВОЗНИКАЕТ ВОПРОС - КАК УСТАНОВИТЬ СВЯЗЬ НАИМЕНОВАНИЯ ТАБЛИЦЫ СО ЗНАЧЕНИЕМ ИЗ chat_list?
--чатГПТ сказал никак, мол колонка может ссылаться только на колонку и нем ожет ссылаться на название таблицы

-- очистка содержимого таблицы
--delete from public.zz1yy5;
-- начальное заполнение
INSERT INTO public.zz1yy5 (mes_author_id, mes_content, mes_datetime, mes_comment)
VALUES
    (1, 'Сообщение 1', '2023-11-26 12:00:01', 'Комментарий к сообщению 1'),
    (5, 'Сообщение 2', '2023-11-26 12:00:02', 'Комментарий к сообщению 2'),
    (5, 'Сообщение 3', '2023-11-26 12:00:03', 'Комментарий к сообщению 3'),
    (1, 'Сообщение 4', '2023-11-26 12:00:04', 'Комментарий к сообщению 4');
-- текущее заполнение
INSERT INTO public.zz1yy5 (mes_author_id, mes_content, mes_datetime, mes_comment)
VALUES
    (1, 'Сообщение 5', '2023-11-26 12:00:05', 'Комментарий к сообщению 5');
   
-- показать всё содержимое
select * from public.zz1yy5;

-- Эта функция создаст уведомление с именем "newmes_zz1yy5" - это для слушателя java: String listenQuery = "LISTEN message_inserted";
-- и передаст в него значения колонок (mes_author_id, mes_content, mes_datetime, mes_comment) новой строки в таблице
CREATE OR REPLACE FUNCTION notify_newmes_zz1yy5() RETURNS trigger AS $$
DECLARE
BEGIN
  PERFORM pg_notify('newmes_zz1yy5', NEW.mes_author_id || '|' || mes_content || '|' || mes_datetime || '|' || mes_comment);
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Этот триггер вызовет функцию "notify_newmes_zz1yy5"
-- после каждой вставки новой строки в таблицу "zz1yy5"
--УДАЛЕНИЕ ТРИГГЕРА
--DROP TRIGGER newmes_zz1yy5_trigger ON public.zz1yy5;
--СОЗДАНИЕ ТРИГГЕРА
CREATE TRIGGER newmes_zz1yy5_trigger
    AFTER INSERT
    ON public.zz1yy5
    FOR EACH ROW
    EXECUTE PROCEDURE public.notify_newmes_zz1yy5();



/*----------------------------------------------------------------*/
/*--- ИСТОРИЯ СООБЩЕНИЙ - это и ниже - всё то, что переделываю (чать тоже переделана, например user) ---*/
-- удаление таблицы
--DROP TABLE public.message_history CASCADE;
-- создание таблицы
CREATE TABLE public.message_history (
	id SERIAL PRIMARY KEY,
	sender_id integer NOT null,
	receiver_id integer NOT null,
	message varchar(1000) null,
	datetime timestamp NOT null
);

-- установка внешних ключей
ALTER TABLE  public.message_history
ADD CONSTRAINT fk_sender_id FOREIGN KEY (sender_id)
REFERENCES public.user (id);
ALTER TABLE  public.message_history
ADD CONSTRAINT fk_receiver_id FOREIGN KEY (receiver_id)
REFERENCES public.user (id);

-- очистка содержимого таблицы
delete from public.message_history;
-- начальное заполнение
INSERT INTO public.message_history
(sender_id, receiver_id, message, datetime)
values
(1, 2, 'Привет, как дела?', '2027-07-07 17:17:00.000'),
(2, 1, 'Привет, нормально, а у тебя?', '2027-07-07 17:17:00.001'),
(1, 2, 'Тоже ничего )', '2027-07-07 17:17:00.002'),
(2, 1, 'Что делаешь?', '2027-07-07 17:17:00.003');

-- текущее заполнение
INSERT INTO public.message_history
(sender_id, receiver_id, message, datetime)
values
(1, 2, 'Кодить пытаюсь, а ты?', '2027-07-07 17:17:00.004');

-- показать всё содержимое
select * from public.message_history;


/****************************************************/
/*******------- СТАРЫЙ РАБОЧИЙ ВАРИАНТ -------*******/

-- создание таблицы сообщений
CREATE TABLE public.messagestable (
	id text NOT NULL,
	mes text NULL
);


-- Эта функция создаст уведомление с именем "message_inserted" 
-- и передаст в него значения колонок "ID" и "mes" новой строки в таблице. 
CREATE OR REPLACE FUNCTION notify_message() RETURNS trigger AS $$
DECLARE
BEGIN
  PERFORM pg_notify('message_inserted', NEW.id || '|' || NEW.mes);
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;


-- Этот триггер вызовет функцию "notify_message"
-- после каждой вставки новой строки в таблицу "messagestable".
CREATE TRIGGER message_insert_trigger
    AFTER INSERT
    ON public.messagestable
    FOR EACH ROW
    EXECUTE PROCEDURE public.notify_message();

   
-- добавление строки
INSERT INTO messagestable (id, mes) VALUES ('тест', 'тест');

-- очистка содержимого таблицы
delete from messagestable;

-- показать всё
select * from messagestable;


-- эксперимент - Можно удалить!
SELECT
	id, mes
FROM
	(SELECT
		id, mes
	FROM
		messagestable
	ORDER BY id DESC LIMIT 20)
AS subquery
ORDER BY id ASC;