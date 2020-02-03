package ru.rt.eip.simpletariffs.validation.repository.itc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.CallableStatement;
import java.sql.Types;
import java.util.Collections;
import java.util.List;

@Slf4j
@Repository
public class ItcRepository {

    @PersistenceContext(unitName = "itcEntityManager")
    private EntityManager entityManager;

    public List<ItcPrefixEntity> findItcPrefixes(String zoneName, String fromDate) {

        Session session = entityManager.unwrap(Session.class);
        DrclId drclId = getDrclId(zoneName, session);

        if (drclId.error == 0) {
            Integer count = unloadDirectionsToTempTable(session, drclId);
            if (count > 0) {

                if (log.isDebugEnabled()) {
                    List<ItcPrefixEntityDebug> itcPrefixesDebug = session
                            .createNamedQuery("getItcPrefixesDebug", ItcPrefixEntityDebug.class)
                            .getResultList();

                    log.debug(itcPrefixesDebug.toString());
                }

                return session.createNamedQuery("getItcPrefixes", ItcPrefixEntity.class)
                              .setParameter("fromDate", fromDate)
                              .getResultList();
            } else {
                return Collections.emptyList();
            }
        } else {
            return Collections.emptyList();
        }
    }

    public List<ItcPrefixesCountEntity> findItcPrefixesCount(String zoneName, String fromDate) {

        Session session = entityManager.unwrap(Session.class);
        DrclId drclId = getDrclId(zoneName, session);

        if (drclId.error == 0) {
            Integer count = unloadDirectionsToTempTable(session, drclId);
            if (count > 0) {

                if (log.isDebugEnabled()) {
                    List<ItcPrefixEntityDebug> itcPrefixesDebug = session
                            .createNamedQuery("getItcPrefixesDebug", ItcPrefixEntityDebug.class)
                            .getResultList();

                    log.debug(itcPrefixesDebug.toString());
                }

                return session.createNamedQuery("getItcPrefixesCount", ItcPrefixesCountEntity.class)
                              .setParameter("fromDate", fromDate)
                              .getResultList();
            } else {
                return Collections.emptyList();
            }
        } else {
            return Collections.emptyList();
        }
    }

    private Integer unloadDirectionsToTempTable(Session session, DrclId drclId) {
        return session.doReturningWork(
                connection -> {
                    try (CallableStatement function = connection
                            .prepareCall("{ ? = call s_itc_api.directions_unload(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
                                                 "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) }")) {

                        function.registerOutParameter(1, Types.INTEGER);

                        function.setNull(2, Types.INTEGER);
                        function.setNull(3, Types.VARCHAR);

                        function.setString(4, "A");

                        function.setNull(5, Types.VARCHAR);
                        function.setNull(6, Types.VARCHAR);
                        function.setNull(7, Types.VARCHAR);
                        function.setNull(8, Types.VARCHAR);
                        function.setNull(9, Types.VARCHAR);
                        function.setNull(10, Types.VARCHAR);
                        function.setNull(11, Types.VARCHAR);
                        function.setNull(12, Types.VARCHAR);

                        function.setInt(13, drclId.getDrclId());

                        function.setNull(14, Types.VARCHAR);
                        function.setNull(15, Types.VARCHAR);
                        function.setNull(16, Types.VARCHAR);
                        function.setNull(17, Types.VARCHAR);
                        function.setNull(18, Types.VARCHAR);
                        function.setNull(19, Types.VARCHAR);
                        function.setNull(20, Types.VARCHAR);
                        function.setNull(21, Types.VARCHAR);
                        function.setNull(22, Types.VARCHAR);
                        function.setNull(23, Types.VARCHAR);
                        function.setNull(24, Types.VARCHAR);
                        function.setNull(25, Types.VARCHAR);
                        function.setNull(26, Types.VARCHAR);
                        function.setNull(27, Types.VARCHAR);
                        function.setNull(28, Types.VARCHAR);

                        function.setString(29, "Y");
                        function.setString(30, "Y");
                        function.setString(31, "Y");

                        function.execute();
                        return function.getInt(1);

                    } catch (Exception e) {
                        log.error("Exception at call s_itc_api.directions_unload", e);
                        return 0;
                    }
                });
    }

    private DrclId getDrclId(String zoneName, Session session) {
        return session.doReturningWork(
                connection -> {
                    try (CallableStatement function = connection
                            .prepareCall("{ ? = call s_itc_api.get_drcl_id(?, ?) }")) {
                        function.registerOutParameter(1, Types.INTEGER);
                        function.registerOutParameter(2, Types.INTEGER);
                        function.setString(3, zoneName /*"hb (TLA) d.ESP IDD Spain-mobile Orange"*/);
                        function.execute();

                        return new DrclId(function.getInt(1), function.getInt(2));
                    }
                });
    }

    @Data
    @AllArgsConstructor
    static class DrclId {
        private int error;
        private int drclId;
    }

}

/* Функция предназначена для выгрузки данных о схеме направлений из БД PETER-SERVICE ITC в промежуточную таблицу API_DIRECTIONS для последующего анализа.
Для префиксов схемонезависимых префиксных зон, включенных в схему направлений, идентификатор данной схемы направлений, ее наименование и описание не выгружаются.
При незаданных параметрах P_DRSC_ID, P_DRSC_NAME осуществляется выгрузка информации только по заданной префиксной зоне без привязки к схеме направлений.
	Формат:
FUNCTION directions_unload (
  p_drsc_id                    direction_schemas.drsc_id%TYPE := NULL,
  p_drsc_name                  direction_schemas.name%TYPE := NULL,
  p_drsc_status                direction_schemas.name%TYPE := c_status_opened,
  p_dssc_id                    distance_schemas.dssc_id%TYPE := NULL,
  p_dssc_name                  distance_schemas.name%TYPE := NULL,
  p_dstn_id                    distance_classes.dstn_id%TYPE := NULL,
  p_dstn_name                  distance_classes.name%TYPE := NULL,
  p_drgr_id                    direction_groups.drgr_id%TYPE := NULL,
  p_drgr_name                  direction_groups.name%TYPE := NULL,
  p_drtp_num                   drcl_types.drtp_num%TYPE := NULL,
  p_cldr_num                   calls_division_rules.cldr_num%TYPE := NULL,
  p_drcl_id                    direction_classes.drcl_id%TYPE := NULL,
  p_drcl_name                  direction_classes.name%TYPE := NULL,
  p_prefix_calls_sharing_yn direction_classes.prefix_calls_sharing_yn%TYPE := NULL,
  p_drcl_descr                 direction_classes.descr%TYPE := NULL,
  p_drcl_short_name            direction_classes.short_name%TYPE := NULL,
  p_drcl_number_detail_yn      direction_classes.number_detail_yn%TYPE := NULL,
  p_ptyp_num                   prefix_types.ptyp_num%TYPE := NULL,
  p_pset_prefix                prefix_sets.prefix%TYPE := NULL,
  p_pset_full_number           prefix_sets.full_number%TYPE := NULL,
  p_pset_descr                 prefix_sets.descr%TYPE := NULL,
  p_pset_id                    prefix_sets.pset_id%TYPE := NULL,
  p_drcm_start_date            dir_composition_hist.start_date%TYPE := NULL,
  p_drcm_end_date              dir_composition_hist.end_date%TYPE := NULL,
  p_drcm_id                    dir_composition_hist.drcm_id%TYPE := NULL,
  p_subs_id                    subscribers.subs_id%TYPE := NULL,
  p_subs_short_name            subscribers.short_name%TYPE := NULL,
  p_unload_id_yn               CHAR := ‘Y’,
  p_truncate_yn                CHAR := ‘Y’,
  p_direct_drcl_yn             CHAR := ‘Y’)
  RETURN                       NUMBER;
	Параметры:
•	p_drsc_id – идентификатор схемы направлений (значение по умолчанию – NULL);
•	p_drsc_name – наименование схемы направлений (значение по умолчанию – NULL);
•	p_drsc_status – состояние схемы направлений:
•	'О’ – доступная схема направлений (значение по умолчанию);
•	‘С’ – недоступная схема направлений;
•	‘А’ – состояние не важно (выгружать все записи);
•	p_dssc_id – идентификатор схемы дистанций (значение по умолчанию – NULL);
•	p_dssc_name – наименование схемы дистанций (значение по умолчанию – NULL);
•	p_dstn_id – идентификатор класса дистанций (значение по умолчанию – NULL);
•	p_dstn_name – наименование класса дистанций (значение по умолчанию – NULL);
•	p_drgr_id – идентификатор группы направлений, которая входит в схему направлений (значение по умолчанию – NULL);
•	p_drgr_name – наименование группы направлений (значение по умолчанию – NULL);
•	p_drgr_descr – описание группы направлений (значение по умолчанию – NULL);
•	p_drtp_num – идентификатор типа префиксной зоны (значение по умолчанию – NULL);
•	p_cldr_num – идентификатор правила деления вызова при пересечении им границ суток и временных зон (значение по умолчанию – NULL);
•	p_drcl_id – идентификатор префиксной зоны (значение по умолчанию – NULL);
•	p_drcl_name – наименование префиксной зоны;
•	p_prefix_calls_sharing_yn – признак необходимости накопления объемов с учетом префикса вызываемого телефонного номера; может принимать значения ‘Y’ или ‘N’ (значение по умолчанию – NULL);
•	p_drcl_descr – комментарий к префиксной зоне (значение по умолчанию – NULL);
•	p_drcl_short_name – краткое наименование префиксной зоны (значение по умолчанию – NULL);
•	p_drcl_number_detail_yn – признак детализации по номерам зоны (значение по умолчанию – NULL);
•	p_ptyp_num – идентификатор типа префикса (значение по умолчанию – NULL);
•	p_pset_prefix – префикс телефонного номера (значение по умолчанию – NULL);
•	p_pset_full_number – способ поиска префикса при тарификации вызова:
•	‘Y’ – префикс применяется только к номерам равной ему длины (поиск по полному номеру);
•	’N’ – префикс применяется к номерам любой длины (поиск по части номера);
значение по умолчанию – NULL;
•	p_pset_descr – комментарий к префиксу (значение по умолчанию – NULL);
•	p_pset_id – идентификатор префикса телефонного номера (значение по умолчанию – NULL);
•	p_drcm_start_date – дата начала вхождения префикса в префиксную зону (значение по умолчанию – NULL);
•	p_drcm_end_date – дата окончания вхождения префикса в префиксную зону (значение по умолчанию – NULL);
•	p_drcm_id – идентификатор записи о вхождении префикса в префиксную зону (значение по умолчанию – NULL);
•	p_subs_id – идентификатор абонента (значение по умолчанию – NULL);
•	p_subs_short_name – краткое наименование абонента (значение по умолчанию – NULL);
•	p_unload_id_yn – флаг, определяющий условия выгрузки данных в промежуточные таблицы:
•	‘Y’ – в таблицу заносятся значения, соответствующие первичным ключам (значение по умолчанию);
•	‘N’ – во все поля, соответствующие первичным ключам, заносится значение NULL;
•	p_truncate_yn – флаг, определяющий действия над промежуточной таблицей:
•	‘Y’ – выполняется предварительная очистка (TRUNCATE) промежуточной таблицы (значение по умолчанию);
•	‘N’ – таблица не очищается;
•	p_direct_drcl_yn – правило выгрузки префиксных зон:
•	‘Y’ – выгружаются только префиксные зоны, непосредственно входящие в схему направлений; если в функцию переданы параметры, определяющие схему направлений, то информация о вложенных префиксных зонах не выгружается (значение по умолчанию);
•	‘N’ – выгружаются все префиксные зоны, входящие в схему направлений, включая вложенные префиксные зоны.
	Результат
Функция возвращает количество строк, выгруженных в промежуточную таблицу.
	Исключения
При возникновении исключительной ситуации функция вызывает обработчик ошибок ORACLE.
	Пример
DECLARE
res INTEGER;
begin
      res := itc_api.directions_unload(p_drsc_id => 2,
                                       p_drsc_name => NULL,
                                       p_drsc_status => NULL,
                                       p_dssc_id => NULL,
                                       p_dssc_name => NULL,
                                       p_dstn_id => NULL,
                                       p_dstn_name => NULL,
                                       p_drgr_id => NULL,
                                       p_drgr_name => NULL,
                                       p_drtp_num => NULL,
                                       p_cldr_num => NULL,
                                       p_drcl_id => NULL,
                                       p_drcl_name => NULL,
                                       p_prefix_calls_sharing_yn => NULL,
                                       p_drcl_descr => NULL,
                                       p_drcl_short_name => NULL,
                                       p_drcl_number_detail_yn => NULL,
                                       p_ptyp_num => NULL,
                                       p_pset_prefix => NULL,
                                       p_pset_full_number => NULL,
                                       p_pset_descr => NULL,
                                       p_pset_id => NULL,
                                       p_drcm_start_date => NULL,
                                       p_drcm_end_date => NULL,
                                       p_drcm_id => NULL,
                                       p_subs_id => NULL,
                                       p_subs_short_name => NULL,
                                       p_unload_id_yn => NULL,
                                       p_truncate_yn => NULL,
                                       p_direct_drcl_yn => 'Y'); */