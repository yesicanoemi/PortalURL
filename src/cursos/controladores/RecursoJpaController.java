/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cursos.controladores;


import Principal.exceptions.NonexistentEntityException; // Excepciones del principal
import cursos.percistence.Recurso;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import cursos.percistence.SeccionCursos;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author Pablito Garzona
 */
public class RecursoJpaController implements Serializable {

    public RecursoJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Recurso recurso) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            SeccionCursos seccionCursoid = recurso.getSeccionCursoid();
            if (seccionCursoid != null) {
                seccionCursoid = em.getReference(seccionCursoid.getClass(), seccionCursoid.getId());
                recurso.setSeccionCursoid(seccionCursoid);
            }
            em.persist(recurso);
            if (seccionCursoid != null) {
                seccionCursoid.getRecursoCollection().add(recurso);
                seccionCursoid = em.merge(seccionCursoid);
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Recurso recurso) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Recurso persistentRecurso = em.find(Recurso.class, recurso.getId());
            SeccionCursos seccionCursoidOld = persistentRecurso.getSeccionCursoid();
            SeccionCursos seccionCursoidNew = recurso.getSeccionCursoid();
            if (seccionCursoidNew != null) {
                seccionCursoidNew = em.getReference(seccionCursoidNew.getClass(), seccionCursoidNew.getId());
                recurso.setSeccionCursoid(seccionCursoidNew);
            }
            recurso = em.merge(recurso);
            if (seccionCursoidOld != null && !seccionCursoidOld.equals(seccionCursoidNew)) {
                seccionCursoidOld.getRecursoCollection().remove(recurso);
                seccionCursoidOld = em.merge(seccionCursoidOld);
            }
            if (seccionCursoidNew != null && !seccionCursoidNew.equals(seccionCursoidOld)) {
                seccionCursoidNew.getRecursoCollection().add(recurso);
                seccionCursoidNew = em.merge(seccionCursoidNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = recurso.getId();
                if (findRecurso(id) == null) {
                    throw new NonexistentEntityException("The recurso with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Recurso recurso;
            try {
                recurso = em.getReference(Recurso.class, id);
                recurso.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The recurso with id " + id + " no longer exists.", enfe);
            }
            SeccionCursos seccionCursoid = recurso.getSeccionCursoid();
            if (seccionCursoid != null) {
                seccionCursoid.getRecursoCollection().remove(recurso);
                seccionCursoid = em.merge(seccionCursoid);
            }
            em.remove(recurso);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Recurso> findRecursoEntities() {
        return findRecursoEntities(true, -1, -1);
    }

    public List<Recurso> findRecursoEntities(int maxResults, int firstResult) {
        return findRecursoEntities(false, maxResults, firstResult);
    }

    private List<Recurso> findRecursoEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Recurso.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Recurso findRecurso(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Recurso.class, id);
        } finally {
            em.close();
        }
    }

    public int getRecursoCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Recurso> rt = cq.from(Recurso.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
