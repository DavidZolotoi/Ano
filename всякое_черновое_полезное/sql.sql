/*-- 1. ЮЗЕРЫ ---*/
-- переименование
--ALTER TABLE public.user RENAME TO "user";
-- удаление таблицы
--DROP TABLE public.user CASCADE;
-- создание таблицы
CREATE TABLE public.user (
	usid SERIAL PRIMARY KEY,
	uslogin varchar(50) NOT null,
	uspassword varchar(50) NOT null,
	usfirstname varchar(50) null,
	uslastname varchar(50) null,
	usemail varchar(50) null,
	usphone varchar(50) null,
	uscomment varchar(500) null
);

-- очистка содержимого таблицы
--delete from public.user;
-- начальное заполнение
INSERT INTO public.user
(uslogin, uspassword, usfirstname, uslastname, usemail, usphone, uscomment)
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
select usid
from public.user
where uslogin = 'Sergey';
-- показать loginЫ по id
select uslogin
from public.user
where usid = 1 or usid = 4 or usid = 7;
-- показать id и password по логину
select usid,uslogin,uspassword 
from public.user
where uslogin = 'Sergey';
-- поискать с фильтром
select usid, uslogin 
from public.user
where uslogin ILIKE '%a%';
-- показать всё содержимое
select * from public.user;

/*-- 2. Список диалогов ---*/
-- удаление таблицы
--DROP TABLE public.chatlist CASCADE;
-- создание таблицы
CREATE TABLE public.chatlist (
	clid SERIAL PRIMARY KEY,
	cluseridmin integer NOT null,
	cluseridmax integer NOT null,
	cltablename varchar(70) null,
	clcomment varchar(500) null
);

-- удаление ключей
--ALTER TABLE public.chatlist DROP CONSTRAINT fkuseridmax;
--ALTER TABLE public.chatlist DROP CONSTRAINT fkuseridmin;
-- установка внешних ключей
ALTER TABLE  public.chatlist
ADD CONSTRAINT fkcluseridmin FOREIGN KEY (cluseridmin)
REFERENCES public.user (usid);
ALTER TABLE  public.chatlist
ADD CONSTRAINT fkcluseridmax FOREIGN KEY (cluseridmax)
REFERENCES public.user (usid);

-- очистка содержимого таблицы
--delete from public.chatlist;
-- начальное заполнение
INSERT INTO public.chatlist
(cluseridmin, cluseridmax, cltablename, clcomment)
values
(1,5,'zz1yy5',null),
(4,7,'zz4yy7',null),
(3,6,'zz3yy6',null),
(5,7,'zz5yy7',null),
(4,5,'zz4yy5',null);


-- Эта функция создаст уведомление для пользователей - это для слушателя java: String listenQuery = "LISTEN ...";
-- и передаст в него значения всех колонок новой строки в таблице
CREATE OR REPLACE FUNCTION fchatlist() RETURNS trigger AS $$
DECLARE
BEGIN
  PERFORM pg_notify('ncl', NEW.clid || '|' || NEW.cluseridmin || '|' || NEW.cluseridmax || '|' || NEW.cltablename || '|' || NEW.clcomment);
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Этот триггер вызовет функцию "fchatlist"
-- после каждой вставки новой строки в таблицу "public.chatlist"
--УДАЛЕНИЕ ТРИГГЕРА
-- DROP TRIGGER tchatlist ON public.chatlist;
--СОЗДАНИЕ ТРИГГЕРА
CREATE TRIGGER tchatlist
    AFTER INSERT
    ON public.chatlist
    FOR EACH ROW
    EXECUTE PROCEDURE public.fchatlist();

-- Удаление: триггер - функция - таблица

-- показать все чаты, где Sergey (id=5)
select *
from public.chatlist
where cluseridmin = 5 or cluseridmax = 5;

-- проверить наличие чата
SELECT clid FROM public.chatlist WHERE cltablename = 'zz5yy7';

-- получить userid_min, userid_max по id для chat_list
select cluseridmin, cluseridmax from public.chatlist where clid = 1;


-- показать всё содержимое
select * from public.chatlist;


/*--- 3. ИСТОРИЯ СООБЩЕНИЙ - ДИАЛОГ ---*/
-- удаление таблицы
--DROP TABLE public.zz1yy5 CASCADE;
-- создание таблицы
CREATE TABLE IF NOT EXISTS public.zz1yy5 (
	zyid SERIAL PRIMARY KEY,
	zyauthorid integer NOT null,
	zycontent varchar(1000) null,
	zydatetime timestamp NOT null,
	zycomment varchar(256) null
);

-- 3.1. установка внешних ключей
ALTER TABLE  public.zz1yy5									--в таблице zz1yy5
ADD CONSTRAINT fkzyauthorid FOREIGN KEY (zyauthorid)	--колонка mes_author_id
REFERENCES public.user (usid);							--это тоже самое, что и колонка id из таблицы user
--todo ВОЗНИКАЕТ ВОПРОС - КАК УСТАНОВИТЬ СВЯЗЬ НАИМЕНОВАНИЯ ТАБЛИЦЫ СО ЗНАЧЕНИЕМ ИЗ chat_list?
--чатГПТ сказал никак, мол колонка может ссылаться только на колонку и нем ожет ссылаться на название таблицы

-- очистка содержимого таблицы
--delete from public.zz1yy5;
-- начальное заполнение
INSERT INTO public.zz1yy5 (zyauthorid, zycontent, zydatetime, zycomment)
VALUES
    (1, 'Сообщение 1', '2023-11-26 12:00:01', 'Комментарий к сообщению 1'),
    (5, 'Сообщение 2', '2023-11-26 12:00:02', 'Комментарий к сообщению 2'),
    (5, 'Сообщение 3', '2023-11-26 12:00:03', 'Комментарий к сообщению 3'),
    (1, 'Сообщение 4', '2023-11-26 12:00:04', 'Комментарий к сообщению 4');
-- текущее заполнение
INSERT INTO public.zz1yy5 (zyauthorid, zycontent, zydatetime, zycomment)
VALUES
    (1, 'Сообщение 5', '2023-11-26 12:00:05', 'Комментарий к сообщению 5');
INSERT INTO public.zz1yy5 (zyauthorid, zycontent, zydatetime, zycomment)
VALUES
    (5, 'Сообщение 6', '2023-11-26 12:00:06', 'Комментарий к сообщению 6');
INSERT INTO public.zz1yy5 (zyauthorid, zycontent, zydatetime, zycomment)
values
	(5, 'Сообщение 7', '2023-12-05 01:25:47.6917193', 'Комментарий к сообщению 7');
INSERT INTO public.zz1yy5 (zyauthorid, zycontent, zydatetime, zycomment)
values
	(1, 'Сообщение 8', '2023-12-05 01:25:49.6917193', 'Комментарий к сообщению 8'); 


-- Эта функция создаст уведомление с именем "nzz1yy5" - это для слушателя java: String listenQuery = "LISTEN nzz1yy5";
-- и передаст в него значения колонок (zyid, zyauthorid, zycontent, zydatetime, zycomment) новой строки в таблице
CREATE OR REPLACE FUNCTION fzz1yy5() RETURNS trigger AS $$
DECLARE
BEGIN
  PERFORM pg_notify('nzz1yy5', NEW.zyid || '|' || NEW.zyauthorid || '|' || NEW.zycontent || '|' || NEW.zydatetime || '|' || NEW.zycomment);
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Этот триггер вызовет функцию "fzz1yy5"
-- после каждой вставки новой строки в таблицу "zz1yy5"
--УДАЛЕНИЕ ТРИГГЕРА
-- DROP TRIGGER tzz1yy5 ON public.zz1yy5;
--СОЗДАНИЕ ТРИГГЕРА
CREATE TRIGGER tzz1yy5
    AFTER INSERT
    ON public.zz1yy5
    FOR EACH ROW
    EXECUTE PROCEDURE public.fzz1yy5();

-- Удаление: триггер - функция - таблица
      
-- показать всё содержимое
select * from public.zz1yy5;

-- загрузить последние 20 сообщений
SELECT
	*
FROM
	(SELECT	* from zz1yy5
	ORDER BY zydatetime DESC LIMIT 20)
AS last_message_not_ordered
ORDER BY zydatetime ASC;
   
   


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