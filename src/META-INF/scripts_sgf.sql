grant connect on database postgis to sgf;
grant usage on schema public to sgf;

select 'grant execute on function public.'||routine_name||'('||(
         select replace(replace(array_to_string(array(select row(case when data_type = 'USER-DEFINED'
            then udt_schema||'.'||udt_name
       else data_type
      end ) from information_schema.parameters p  
   where specific_schema='public'
   and p.specific_name = r.specific_name order by specific_name,ordinal_position),','),'(',''),')',''))
||') to sgf;' from information_schema.routines r where routine_schema='public'
order by 1;

-- psql -d pmf_desv -U sgf -h 172.30.116.22 -f pontos2.sql
-- set search_path=sgf, fortalfa, pg_catalog

--insert into tb_ponto(descponto, geomponto) values ('p1',geomfromtext('POINT(-3.728040459764619 -38.52433204650879)'));
--insert into tb_ponto(descponto, geomponto) values ('p2',geomfromtext('POINT(-3.730138859318468 -38.516950607299805)'));
--insert into tb_ponto(descponto, geomponto) values ('p3',geomfromtext('POINT(-3.738682291459722 -38.52452516555786)'));
--insert into tb_transmissao(dat_transmissao,distancia_pontoprox,geomponto,odometro,temperatura,velocidade,codponto,codveiculo) values ('2010-08-13 10:37:00', 2345, geomfromtext('POINT(-3.744463514117082 -38.53506088256836)'), NULL, NULL,NULL,2, 12);
--insert into tb_transmissao(dat_transmissao,distancia_pontoprox,geomponto,odometro,temperatura,velocidade,codponto,codveiculo) values ('2010-08-13 10:41:00', 2345, geomfromtext('POINT(-3.7505230509601346 -38.52800130844116)'), NULL, NULL,NULL,2, 12);
--insert into tb_transmissao(dat_transmissao,distancia_pontoprox,geomponto,odometro,temperatura,velocidade,codponto,codveiculo) values ('2010-08-13 10:45:00', 2345, geomfromtext('POINT(-3.752685631679395 -38.51383924484253)'), NULL, NULL,NULL,2, 12);

insert into tb_ponto(descponto, x, y) values ('p1',-3.728040459764619, -38.52433204650879);
insert into tb_ponto(descponto, x, y) values ('p2',-3.730138859318468, -38.516950607299805);
insert into tb_ponto(descponto, x, y) values ('p3',-3.738682291459722, -38.52452516555786);

insert into tb_transmissao(dat_transmissao,distancia_pontoprox,x,y,odometro,temperatura,velocidade,codponto,codveiculo) values ('2010-08-13 10:37:00', 2345, -3.744463514117082, -38.53506088256836, NULL, NULL,NULL,2, 2);
insert into tb_transmissao(dat_transmissao,distancia_pontoprox,x,y,odometro,temperatura,velocidade,codponto,codveiculo) values ('2010-08-13 10:41:00', 2345, -3.7505230509601346, -38.52800130844116, NULL, NULL,NULL,2, 2);
insert into tb_transmissao(dat_transmissao,distancia_pontoprox,x,y,odometro,temperatura,velocidade,codponto,codveiculo) values ('2010-08-13 10:45:00', 2345,-3.752685631679395, -38.51383924484253, NULL, NULL,NULL,2, 2);


-- TABELAS P/ PREVIEW DO RESTREAMENTO DE VEÍCULO

CREATE TABLE sgf.tb_ponto
(
  codponto serial NOT NULL,
  descponto character varying(255),
  --geomponto fortalfa.geometry,
  x float,
  y float,
  z float,
  CONSTRAINT tb_ponto_pkey PRIMARY KEY (codponto)
);
ALTER TABLE sgf.tb_ponto OWNER TO sgf;


CREATE TABLE sgf.tb_ultimatransmissao
(
  dat_transmissao timestamp without time zone,
  distancia_pontoprox numeric,
  --geomponto fortalfa.geometry,
  x float,
  y float,
  z float,
  odometro float,
  temperatura float,
  velocidade float,
  codpontoprox integer,
  codveiculo integer NOT NULL,
  CONSTRAINT tb_ultimatransmissao_pkey PRIMARY KEY (codveiculo),
  CONSTRAINT fk636321419ccd7e34 FOREIGN KEY (codpontoprox)
      REFERENCES sgf.tb_ponto (codponto) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk63632141e4286893 FOREIGN KEY (codveiculo)
      REFERENCES sgf.tb_cadveiculo (codveiculo) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);
ALTER TABLE sgf.tb_ultimatransmissao OWNER TO sgf;


CREATE TABLE sgf.tb_transmissao
(
  codtransmissao serial NOT NULL,
  dat_transmissao timestamp without time zone,
  distancia_pontoprox numeric,
  --geomponto fortalfa.geometry,
  x float,
  y float,
  z float,
  odometro float,
  temperatura float,
  velocidade float,
  codponto integer,
  codveiculo integer NOT NULL,
  CONSTRAINT tb_transmissao_pkey PRIMARY KEY (codtransmissao),
  CONSTRAINT fk636321419dcd7e34 FOREIGN KEY (codponto)
      REFERENCES sgf.tb_ponto (codponto) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk636321a1e4286893 FOREIGN KEY (codveiculo)
      REFERENCES sgf.tb_cadveiculo (codveiculo) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);
ALTER TABLE sgf.tb_transmissao OWNER TO sgf;

-- TRIGGER P/ ATUALIZAR A ULTIMA-TRANSMISSÃO DO VEÍCULO
CREATE OR REPLACE FUNCTION sgf.update_ultimatransmissao_veiculo() RETURNS TRIGGER AS $ultima_transmissao$
    DECLARE
	VEIC_ID INTEGER;
	DIST_PONTO NUMERIC;
	PONTO_ID INTEGER;
	X_PT NUMERIC;
	Y_PT NUMERIC;
	ULT_T INTEGER;
    BEGIN
        IF (TG_OP = 'UPDATE' or TG_OP = 'INSERT') THEN
	   /*Cálculo do ponto mais próximo, e da distância*/
        SELECT 12756200*ASIN(SQRT(POWER(SIN((NEW.X-P.X)*pi()/180/2),2)+COS(NEW.X*pi()/180)*COS(P.X*pi()/180)*POWER(SIN((NEW.Y-P.Y)*pi()/180/2),2))) D, P.CODPONTO
	    INTO DIST_PONTO, PONTO_ID
	    FROM SGF.TB_PONTO P ORDER BY D
	    LIMIT 1;

	    SELECT COUNT(1) INTO ULT_T FROM SGF.TB_ULTIMATRANSMISSAO WHERE CODVEICULO = NEW.CODVEICULO;
	    SELECT CODVEICULO INTO VEIC_ID FROM SGF.TB_TRANSMISSAO WHERE CODVEICULO = NEW.CODVEICULO;

	    IF(ULT_T = 0) THEN
		    INSERT INTO SGF.TB_ULTIMATRANSMISSAO(DAT_TRANSMISSAO, DISTANCIA_PONTOPROX, X,Y, ODOMETRO, 
		    TEMPERATURA, VELOCIDADE, CODPONTOPROX, CODVEICULO) VALUES (NEW.DAT_TRANSMISSAO, DIST_PONTO, 
		   -- GEOMFROMTEXT('POINT('|| NEW.X || ' ' || Y(NEW.GEOMPONTO) || ')'), NEW.ODOMETRO, 
		   NEW.X, NEW.Y, NEW.ODOMETRO,NEW.TEMPERATURA, NEW.VELOCIDADE, PONTO_ID, VEIC_ID);
	    ELSE 
	        UPDATE SGF.TB_ULTIMATRANSMISSAO SET  DAT_TRANSMISSAO = NEW.DAT_TRANSMISSAO, DISTANCIA_PONTOPROX = DIST_PONTO,
	        X = NEW.X,  Y = NEW.Y, ODOMETRO = NEW.ODOMETRO, TEMPERATURA = NEW.TEMPERATURA, VELOCIDADE = NEW.VELOCIDADE, 
	        CODPONTOPROX = PONTO_ID WHERE CODVEICULO = VEIC_ID;
	    END IF;
            RETURN NEW;
        END IF;
        RETURN NULL;
    END;
$ultima_transmissao$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_ultimatransmissao_veiculo
AFTER INSERT OR UPDATE ON sgf.tb_transmissao
    FOR EACH ROW EXECUTE PROCEDURE sgf.update_ultimatransmissao_veiculo();
    
-- TRIGGER P/ ATUALIZAR O KILOMETRO ATUAL DO VEICULO DE ACORDO COM AS INFORMAÇÕES DE KM DAS
-- SOLICITAÇÕES DE VEICULO

CREATE OR REPLACE FUNCTION update_km_veiculo() RETURNS TRIGGER AS $km_atual$
    DECLARE
	VEIC_ID INTEGER;
    BEGIN
        IF (TG_OP = 'UPDATE' or TG_OP = 'INSERT') THEN
            SELECT CODVEICULO INTO VEIC_ID FROM SGF.TB_SOLVEICULOS WHERE CODSOLVEICULO = NEW.CODSOLVEICULO;
	    IF(NEW.KMRETORNO <> NULL) THEN
		UPDATE TB_CADVEICULO SET KMATUAL =  NEW.KMRETORNO WHERE CODVEICULO = VEIC_ID;
            ELSE
		UPDATE TB_CADVEICULO SET KMATUAL =  NEW.KMSAIDA WHERE CODVEICULO = VEIC_ID;
            END IF;		
            RETURN NEW;
        END IF;
        RETURN NULL;
    END;
$km_atual$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_km_veiculo
AFTER INSERT OR UPDATE ON sgf.tb_registroveiculos
    FOR EACH ROW EXECUTE PROCEDURE update_km_veiculo();  
    
